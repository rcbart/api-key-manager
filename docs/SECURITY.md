# Security Model

## API key security

**Key generation:** 32 bytes from `java.security.SecureRandom` encoded with URL-safe Base64 (no padding). This gives 256 bits of entropy. Brute forcing the key space is computationally infeasible.

**Key storage:** The raw key is returned exactly once (in the HTTP response immediately after creation) and is never written to the database or any log file. The database stores only a SHA-256 hex digest. SHA-256 without a salt is appropriate here because the key has 256 bits of pre-existing entropy — a pre-computation attack (rainbow table) would require 2^256 SHA-256 computations.

**Display prefix:** The first 6 characters of the random portion are stored as a display prefix so admins can recognize a key in the list. This prefix does not help an attacker narrow the keyspace because 6 characters out of 43 still leaves 37 characters (≈222 bits) unknown.

## Admin authentication

**Passwords** are hashed with BCrypt (Spring Security default strength 10), which is intentionally slow to resist brute force on the hashed password database.

**Timing attack resistance:** The login endpoint always calls `BCryptPasswordEncoder.matches()` — even for usernames that do not exist (using a stored dummy hash) — to prevent an attacker from determining whether a username is valid by measuring response time.

**JWTs** are signed with HS256. The signing key must be at least 32 bytes. On startup, the application throws if the key is too short and logs a prominent warning if the insecure placeholder value is used. JWTs expire after a configurable period (default 60 minutes).

**Session storage:** The frontend stores JWTs in `sessionStorage`, not `localStorage`. This means the token is not accessible to scripts in other tabs and is automatically cleared when the browser tab is closed, reducing the XSS exposure window.

## Transport security

The application itself does not terminate TLS. In production, place a TLS-terminating reverse proxy (nginx, Caddy, Traefik, or a cloud load balancer) in front of the application. All traffic between the browser and your service must be HTTPS to protect API keys in transit.

## CORS

Allowed origins are explicitly configured via `CORS_ORIGIN` (no wildcard). Only `GET`, `POST`, `PATCH`, `DELETE`, and `OPTIONS` are allowed. The `Authorization` and `X-API-Key` headers are explicitly allowed.

## Input validation

Every controller endpoint validates all inputs using Jakarta Bean Validation:
- Key names: 1–255 non-blank characters
- Scope strings: pattern `^[A-Za-z0-9_:.-]{1,150}$`
- IP/CIDR strings: validated by regex before parsing with `InetAddress` for bitmask operations

Malformed or out-of-range inputs return HTTP 400 with a `VALIDATION_ERROR` code. No user-supplied string is used in SQL queries (all queries use JPA repositories with parameterised queries).

## API endpoint exposure

- `POST /api/admin/auth/login` — public (rate limiting at the reverse proxy level is recommended in production)
- `POST /api/validate` — public (intended to be called by your own services on your network; consider placing this behind a network-level control if your threat model requires it)
- `/api/admin/**` — requires valid JWT
- `/actuator/health` and `/actuator/health/**` — public (only `health` is exposed; no sensitive actuator endpoints)
- All other paths — denied (`denyAll()`)

## Rate limiting

The built-in rate limiter is in-memory and per-key. It protects against runaway key usage but does not protect the validation endpoint itself from high-volume abuse. For public-facing deployments, add rate limiting at the reverse proxy or API gateway layer.

## Secrets management

All secrets (`POSTGRES_PASSWORD`, `JWT_SECRET`, `ADMIN_PASSWORD`) are read from environment variables, not hardcoded. The `.env` file containing these values is listed in `.gitignore` and must never be committed to version control. In production, prefer a secrets manager (AWS Secrets Manager, HashiCorp Vault, Kubernetes Secrets) over plain `.env` files.

## Known limitations (v0.1.0)

- Rate limiting is single-instance (in-memory). Running multiple backend replicas without a shared store (e.g. Redis) means each instance tracks rate limits independently.
- There is no built-in admin account management UI — additional admin accounts must be inserted directly into `admin_users` with a bcrypt-hashed password, or the application extended with an admin-management endpoint.
- No IP-based blocking of the admin login endpoint is built in — deploy behind a reverse proxy that can enforce this if needed.
