require('dotenv').config();

const path = require('path');
const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { pool, initialiseDatabase } = require('./db');

const app = express();
const port = Number(process.env.PORT || 3000);
const jwtSecret = process.env.JWT_SECRET;

if (!jwtSecret) {
  console.error('JWT_SECRET is missing. Add it to your .env file before starting the server.');
  process.exit(1);
}

app.use(express.json());
// Avoid stale frontend files during local development after code changes.
app.use(express.static(path.join(__dirname, 'frontend'), {
  setHeaders: response => response.setHeader('Cache-Control', 'no-store')
}));

function validEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function createToken(user) {
  return jwt.sign({ userId: user.id, email: user.email }, jwtSecret, { expiresIn: '8h' });
}

function requireAuth(req, res, next) {
  const token = req.headers.authorization?.replace(/^Bearer\s+/i, '');
  if (!token) return res.status(401).json({ message: 'Please sign in to continue.' });
  try {
    req.user = jwt.verify(token, jwtSecret);
    return next();
  } catch {
    return res.status(401).json({ message: 'Your session has expired. Please sign in again.' });
  }
}

app.post('/api/auth/signup', async (req, res) => {
  const email = String(req.body.email || '').trim().toLowerCase();
  const password = String(req.body.password || '');
  if (!validEmail(email)) return res.status(400).json({ message: 'Enter a valid email address.' });
  if (password.length < 8) return res.status(400).json({ message: 'Password must be at least 8 characters.' });

  try {
    const passwordHash = await bcrypt.hash(password, 12);
    const result = await pool.query(
      'INSERT INTO users (email, password_hash) VALUES ($1, $2) RETURNING id, email',
      [email, passwordHash]
    );
    const user = result.rows[0];
    return res.status(201).json({ token: createToken(user), user: { email: user.email } });
  } catch (error) {
    if (error.code === '23505') return res.status(409).json({ message: 'An account already exists for this email.' });
    console.error('Signup error:', error);
    return res.status(500).json({ message: 'Unable to create your account. Please try again.' });
  }
});

app.post('/api/auth/login', async (req, res) => {
  const email = String(req.body.email || '').trim().toLowerCase();
  const password = String(req.body.password || '');
  try {
    const result = await pool.query('SELECT id, email, password_hash FROM users WHERE email = $1', [email]);
    const user = result.rows[0];
    if (!user || !(await bcrypt.compare(password, user.password_hash))) {
      return res.status(401).json({ message: 'Incorrect email or password.' });
    }
    return res.json({ token: createToken(user), user: { email: user.email } });
  } catch (error) {
    console.error('Login error:', error);
    return res.status(500).json({ message: 'Unable to log in. Please try again.' });
  }
});

app.get('/api/parking', requireAuth, async (req, res) => {
  try {
    const [activeResult, historyResult, revenueResult] = await Promise.all([
      pool.query(`SELECT id, vehicle_number, owner_name, vehicle_type, slot_no, entry_time
                  FROM parking_visits WHERE user_id = $1 AND exit_time IS NULL ORDER BY entry_time DESC`, [req.user.userId]),
      pool.query(`SELECT id, vehicle_number, owner_name, vehicle_type, slot_no, entry_time, exit_time, fee
                  FROM parking_visits WHERE user_id = $1 AND exit_time IS NOT NULL ORDER BY exit_time DESC`, [req.user.userId]),
      pool.query(`SELECT COALESCE(SUM(fee), 0) AS revenue FROM parking_visits
                  WHERE user_id = $1 AND exit_time IS NOT NULL`, [req.user.userId])
    ]);
    return res.json({ active: activeResult.rows, history: historyResult.rows, revenue: Number(revenueResult.rows[0].revenue) });
  } catch (error) {
    console.error('Parking data error:', error);
    return res.status(500).json({ message: 'Unable to load parking data.' });
  }
});

app.post('/api/parking', requireAuth, async (req, res) => {
  const number = String(req.body.number || '').trim().toUpperCase();
  const owner = String(req.body.owner || '').trim();
  const type = String(req.body.type || '').toUpperCase();
  if (!number || !owner || !['2W', '4W'].includes(type)) return res.status(400).json({ message: 'Enter valid vehicle details.' });

  try {
    const occupied = await pool.query(
      'SELECT slot_no FROM parking_visits WHERE user_id = $1 AND vehicle_type = $2 AND exit_time IS NULL',
      [req.user.userId, type]
    );
    const usedSlots = new Set(occupied.rows.map(row => row.slot_no));
    const slot = Array.from({ length: 100 }, (_, index) => index + 1).find(slotNo => !usedSlots.has(slotNo));
    if (!slot) return res.status(409).json({ message: `No ${type} slots are available.` });
    const result = await pool.query(
      `INSERT INTO parking_visits (user_id, vehicle_number, owner_name, vehicle_type, slot_no)
       VALUES ($1, $2, $3, $4, $5)
       RETURNING id, vehicle_number, owner_name, vehicle_type, slot_no, entry_time`,
      [req.user.userId, number, owner, type, slot]
    );
    return res.status(201).json({ vehicle: result.rows[0] });
  } catch (error) {
    if (error.code === '23505') return res.status(409).json({ message: 'This vehicle is already parked.' });
    console.error('Parking creation error:', error);
    return res.status(500).json({ message: 'Unable to allocate a parking slot.' });
  }
});

app.post('/api/parking/:id/checkout', requireAuth, async (req, res) => {
  const id = Number(req.params.id);
  if (!Number.isInteger(id)) return res.status(400).json({ message: 'Invalid parking record.' });
  try {
    const result = await pool.query(
      `UPDATE parking_visits
       SET exit_time = NOW(), fee = GREATEST(1, CEIL(EXTRACT(EPOCH FROM (NOW() - entry_time)) / 60))
         * CASE WHEN vehicle_type = '2W' THEN 1 ELSE 2 END
       WHERE id = $1 AND user_id = $2 AND exit_time IS NULL
       RETURNING id, vehicle_number, fee`,
      [id, req.user.userId]
    );
    if (!result.rows[0]) return res.status(404).json({ message: 'Active parking record not found.' });
    return res.json({ vehicle: result.rows[0] });
  } catch (error) {
    console.error('Checkout error:', error);
    return res.status(500).json({ message: 'Unable to check out this vehicle.' });
  }
});

app.get('/api/health', (_req, res) => res.json({ status: 'ok' }));

// Keep API errors in JSON so the browser can always show a useful message.
app.use('/api', (_req, res) => res.status(404).json({ message: 'API endpoint not found.' }));
app.get('*', (_req, res) => res.sendFile(path.join(__dirname, 'frontend', 'index.html')));

initialiseDatabase()
  .then(() => app.listen(port, () => console.log(`ParkEase is running at http://localhost:${port}`)))
  .catch(error => {
    console.error('Could not connect to PostgreSQL. Check your .env database settings.', error.message);
    process.exit(1);
  });
