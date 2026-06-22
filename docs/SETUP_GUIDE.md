# Setup Guide

## Prerequisites

| Tool | Version | Notes |
|---|---|---|
| Java | 17+ | JDK required for development, JRE for runtime |
| Maven | 3.9+ | Or use the included `./mvnw` wrapper |
| Node.js | 20+ | For frontend development |
| PostgreSQL | 14+ | For local development (or use Docker Compose) |
| Docker + Compose | 24+ | For container deployment |

---

## Local development setup

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/api-key-manager.git
cd api-key-manager
```

### 2. Create the PostgreSQL database

```sql
CREATE DATABASE apikeymanager;
CREATE USER apikey WITH PASSWORD 'your_dev_password';
GRANT ALL PRIVILEGES ON DATABASE apikeymanager TO apikey;
```

### 3. Configure the backend

Create `backend/src/main/resources/application-local.yml` (this file is gitignored):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/apikeymanager
    username: apikey
    password: your_dev_password

jwt:
  secret: "local_dev_secret_at_least_32_chars_long"
  expiration-minutes: 1440

admin:
  bootstrap:
    username: admin
    password: localdevpassword

cors:
  allowed-origins: "http://localhost:5173"
```

Then activate the profile:

```bash
export SPRING_PROFILES_ACTIVE=local
```

Alternatively, set environment variables directly:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/apikeymanager
export DATABASE_USERNAME=apikey
export DATABASE_PASSWORD=your_dev_password
export JWT_SECRET=local_dev_secret_at_least_32_chars_long
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=localdevpassword
```

### 4. Run the backend

```bash
cd backend
./mvnw spring-boot:run
# Backend starts on http://localhost:8080
```

Flyway migrations run automatically on first start and create all required tables.

### 5. Run the frontend

```bash
cd frontend
npm install
npm run dev
# Frontend starts on http://localhost:5173
# API calls to /api/* are proxied to http://localhost:8080
```

Open http://localhost:5173 in your browser and log in with the admin credentials you set above.

---

## Running tests

### Backend

```bash
cd backend
./mvnw test
```

Tests use an in-memory H2 database — no PostgreSQL required for the test suite.

### Frontend

```bash
cd frontend
npm run test        # run once and exit
npm run test:watch  # watch mode
```

---

## Environment variable reference

All variables read by the backend. Defaults are shown; a blank default means the value is required.

| Variable | Default | Description |
|---|---|---|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/api_key_manager` | JDBC URL |
| `DATABASE_USERNAME` | `api_key_manager` | Database user |
| `DATABASE_PASSWORD` | `api_key_manager` | Database password |
| `JWT_SECRET` | insecure placeholder (warns on startup) | HS256 signing key, min 32 bytes |
| `JWT_EXPIRATION_MINUTES` | `720` | How long admin JWT tokens last |
| `ADMIN_USERNAME` | _(empty)_ | Bootstrap admin username (used once on fresh DB) |
| `ADMIN_PASSWORD` | _(empty)_ | Bootstrap admin password |
| `CORS_ORIGIN` | `http://localhost:5173` | Origin(s) allowed for CORS, comma-separated |
| `API_KEY_PREFIX` | `ak_live_` | Prefix prepended to all generated keys |
| `LOG_LEVEL` | `INFO` | Log level for `com.apikeymanager.*` packages |
| `PORT` | `8080` | HTTP server port |
