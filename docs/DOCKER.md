# Docker Container Guide

The project ships with a `docker-compose.yml` that runs three containers: PostgreSQL, the Spring Boot backend, and the React frontend served by nginx. All application traffic enters through the frontend container on port 80; nginx proxies `/api/*` requests to the backend internally.

```
Browser ──► nginx :80 ──► /api/* ──► Spring Boot :8080 ──► PostgreSQL :5432
```

---

## Prerequisites

- Docker Engine 24+
- Docker Compose v2 (the `docker compose` subcommand, not the old `docker-compose`)

---

## Step-by-step: run with Docker Compose

### 1. Copy and fill in the environment file

```bash
cp .env.example .env
```

Open `.env` in a text editor and set every value:

```env
POSTGRES_USER=apikey
POSTGRES_PASSWORD=<strong random password>

JWT_SECRET=<at least 32 random characters>
JWT_EXPIRATION_MINUTES=60

ADMIN_USERNAME=admin
ADMIN_PASSWORD=<strong admin password>

CORS_ORIGIN=http://localhost        # change to your public URL in production

FRONTEND_PORT=80                    # host port for the UI
```

Generate a strong JWT secret:

```bash
openssl rand -base64 48
```

> **Never commit `.env` to git.** It is listed in `.gitignore` by default.

### 2. Build and start the stack

```bash
docker compose up --build -d
```

This builds the backend and frontend images, starts PostgreSQL, waits for it to be healthy, starts the backend (Flyway migrations run automatically), then starts the frontend.

The first build takes a few minutes (Maven downloads dependencies). Subsequent builds that only change source files are much faster due to Docker layer caching.

### 3. Open the app

```
http://localhost
```

Log in with the `ADMIN_USERNAME` / `ADMIN_PASSWORD` you set in `.env`.

### 4. Check container status

```bash
docker compose ps
docker compose logs backend     # backend logs
docker compose logs frontend    # nginx logs
docker compose logs postgres    # database logs
```

### 5. Stop the stack

```bash
docker compose down             # stops containers, keeps the postgres_data volume
docker compose down -v          # also removes the volume (data loss!)
```

---

## Updating to a new version

```bash
git pull
docker compose up --build -d
```

Docker Compose rebuilds only the images that changed. Flyway applies any new migrations automatically on backend startup.

---

## Running on a production server

1. Set `CORS_ORIGIN` to your HTTPS domain (e.g. `https://keys.example.com`).
2. Put a TLS-terminating reverse proxy (Caddy, nginx, Traefik, or a cloud load balancer) in front of port 80. The app itself does not handle TLS.
3. Change `FRONTEND_PORT` to `127.0.0.1:8081` (or similar) if your external proxy binds port 80 itself.
4. Set strong, randomly generated `POSTGRES_PASSWORD`, `JWT_SECRET`, and `ADMIN_PASSWORD`.
5. Consider restricting `ADMIN_USERNAME` / `ADMIN_PASSWORD` to empty after the first admin account is bootstrapped (the bootstrap runner ignores these env vars once an admin exists).

Example Caddyfile snippet:

```
keys.example.com {
    reverse_proxy localhost:80
}
```

---

## Building images individually

### Backend only

```bash
cd backend
docker build -t api-key-manager-backend:0.1.0 .
```

### Frontend only

```bash
cd frontend
docker build -t api-key-manager-frontend:0.1.0 .
```

---

## Troubleshooting

**Backend container exits immediately:**
Check `docker compose logs backend`. The most common causes are a missing or short `JWT_SECRET`, missing database credentials, or the database not being ready yet (the `depends_on` health check should prevent this, but migrations also run at startup).

**"Cannot connect to database":**
Ensure `POSTGRES_USER`, `POSTGRES_PASSWORD`, and the database name (`apikeymanager` by default) all match between the postgres and backend services.

**Frontend shows blank page or 502:**
The backend may still be starting. Wait 30–60 seconds and refresh. Check `docker compose logs backend` for errors.
