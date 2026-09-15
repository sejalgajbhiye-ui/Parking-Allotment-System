# ParkEase - Parking Allotment System

ParkEase is a parking management project with a responsive web dashboard, secure user authentication, and a PostgreSQL database. It also retains the original Java console and desktop implementations built with custom data structures.

## Features

- Create an account and sign in using email and password
- Passwords stored securely with bcrypt hashing
- PostgreSQL-backed user accounts
- Dashboard showing active vehicles, available slots, and total revenue
- Allocate slots for two-wheelers and four-wheelers
- Search active vehicles and check them out
- Parking history and fee calculation
- Responsive HTML, CSS, and JavaScript interface

## Tech Stack

- Frontend: HTML, CSS, JavaScript
- Backend: Node.js, Express.js
- Database: PostgreSQL
- Authentication: bcryptjs and JSON Web Tokens (JWT)
- Legacy application: Java, Swing, Queue, Hash Table, ArrayList

## Project Structure

```text
ParkingAllotmentSystem/
├── frontend/
│   ├── index.html          # Login, sign-up, and dashboard UI
│   ├── styles.css          # Responsive styling
│   ├── app.js              # Parking dashboard interactions
│   └── auth.js             # Login and sign-up interactions
├── server.js               # Express server and authentication routes
├── db.js                   # PostgreSQL connection and users table setup
├── package.json            # Node.js dependencies and scripts
├── .env.example            # Database configuration template
├── ParkingSystem.java      # Original Java console application
├── ParkingSystemUI.java    # Java Swing desktop interface
└── README.md
```

## Run the Web Application

### Prerequisites

- Node.js 18 or later
- PostgreSQL running locally

### Setup

1. Install dependencies:

   ```powershell
   npm install
   ```

2. Copy `.env.example` to `.env` and update it with your PostgreSQL settings:

   ```env
   PORT=3000
   DB_HOST=localhost
   DB_PORT=5432
   DB_NAME=postgres
   DB_USER=postgres
   DB_PASSWORD=your_postgres_password
   JWT_SECRET=replace_with_a_long_random_secret
   ```

3. Start the application:

   ```powershell
   npm start
   ```

4. Open [http://localhost:3000](http://localhost:3000) in a browser.

The server creates the `users` table automatically when it first connects to PostgreSQL. Do not open `frontend/index.html` directly or use Live Server, because authentication requires the Node.js backend.

## User Flow

1. Create an account with an email and a password of at least eight characters.
2. After successful sign-up, sign in with the same credentials.
3. The main parking dashboard opens after login.

## Run the Java Application (Optional)

Compile the Java desktop version:

```powershell
javac ParkingSystem.java ParkingSystemUI.java
java ParkingSystem
```

To run the original menu-driven console version:

```powershell
java ParkingSystem --console
```

## Security Notes

- Passwords are hashed before storage; plain-text passwords are never saved.
- The `.env` file is excluded from Git. Keep database credentials and `JWT_SECRET` private.

## Author

Sejal Gajbhiye
