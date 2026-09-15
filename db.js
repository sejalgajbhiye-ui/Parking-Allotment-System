const { Pool } = require('pg');

const pool = new Pool({
  host: process.env.DB_HOST || 'localhost',
  port: Number(process.env.DB_PORT || 5432),
  database: process.env.DB_NAME || 'postgres',
  user: process.env.DB_USER || 'postgres',
  password: process.env.DB_PASSWORD
});

async function initialiseDatabase() {
  await pool.query(`
    CREATE TABLE IF NOT EXISTS users (
      id SERIAL PRIMARY KEY,
      email VARCHAR(255) UNIQUE NOT NULL,
      password_hash VARCHAR(255) NOT NULL,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    )
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS parking_visits (
      id SERIAL PRIMARY KEY,
      user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      vehicle_number VARCHAR(30) NOT NULL,
      owner_name VARCHAR(120) NOT NULL,
      vehicle_type VARCHAR(2) NOT NULL CHECK (vehicle_type IN ('2W', '4W')),
      slot_no INTEGER NOT NULL CHECK (slot_no BETWEEN 1 AND 100),
      entry_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      exit_time TIMESTAMPTZ,
      fee NUMERIC(10, 2) NOT NULL DEFAULT 0
    )
  `);
  await pool.query(`
    CREATE UNIQUE INDEX IF NOT EXISTS unique_active_vehicle_per_user
    ON parking_visits (user_id, vehicle_number) WHERE exit_time IS NULL
  `);
}

module.exports = { pool, initialiseDatabase };
