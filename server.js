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
