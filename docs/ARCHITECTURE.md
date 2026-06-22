# Architecture

## Overview

API Key Manager is a three-tier application:

```
┌─────────────────────────────────────────────────────────────────┐
│  Browser (React + Vite / nginx in Docker)                       │
│  ├── Admin UI: login, list keys, create, edit, revoke, delete   │
│  └── POST /api/validate caller: downstream services             │
└────────────────────┬────────────────────────────────────────────┘
                     │ HTTP (nginx proxy or Vite proxy in dev)
┌────────────────────▼────────────────────────────────────────────┐
│  Spring Boot 3.2 (Java 17)                                      │
│  ├── SecurityConfig: JWT filter + CORS + stateless session      │
│  ├── AuthController: POST /api/admin/auth/login                 │
│  ├── ApiKeyAdminController: CRUD under /api/admin/keys          │
│  ├── ValidationController: POST /api/validate (public)          │
│  ├── KeyGenerationService: SecureRandom + SHA-256               │
│  ├── ValidationService: revocation / expiry / rate / IP / scope │
│  ├── RateLimiterService: in-memory fixed-window per key         │
│  └── IpMatcher: CIDR bitmask, IPv4 + IPv6                       │
└────────────────────┬────────────────────────────────────────────┘
                     │ JDBC (Spring Data JPA + Flyway)
┌────────────────────▼────────────────────────────────────────────┐
│  PostgreSQL 16                                                   │
│  ├── admin_users (id, username, password_hash, created_at)      │
│  ├── api_keys (id, name, display_prefix, key_hash UNIQUE, ...)  │
│  ├── api_key_scopes (api_key_id, scope)                         │
│  └── api_key_allowed_ips (api_key_id, ip_or_cidr)              │
└─────────────────────────────────────────────────────────────────┘
```

---

## Key design decisions

### API key generation and storage

Keys are 32 bytes from `SecureRandom`, encoded with URL-safe Base64 (no padding) → 43-character strings prefixed with `ak_live_`. This gives 256 bits of entropy — brute force is computationally infeasible.

The full raw key is returned to the caller exactly once (on creation) and is never stored. The database stores only a SHA-256 hex digest of the key (`key_hash` column, unique-indexed). SHA-256 (non-salted) is appropriate here because the key has 256 bits of entropy; a rainbow table attack would require pre-computing 2^256 digests, which is impossible. bcrypt (with its intentional slowness) is reserved for human-chosen passwords where entropy is low.

A short "display prefix" (the first 6 characters of the random portion) is stored in plaintext alongside the hash so admins can recognise a key in the list without the full secret being retrievable from the database.

### Admin authentication

Standard username/password login returns a short-lived JWT (HS256, 60-minute default). The JWT is stored in `sessionStorage` (not `localStorage`) in the browser — it is inaccessible from other browser tabs and is cleared when the tab is closed, which reduces the XSS exposure window relative to `localStorage`. The backend is fully stateless; the JWT is verified on every request.

To prevent timing-based username enumeration, the login handler always runs `BCryptPasswordEncoder.matches()` (including a dummy bcrypt call for unknown usernames) before returning a response, ensuring both valid and invalid usernames take similar time.

### Input validation

Every controller `@RequestBody` is annotated with `@Valid`. Request DTOs use Jakarta Bean Validation annotations (`@NotBlank`, `@Pattern`, `@Size`, container-element `@Pattern` for collections). Regex patterns for scopes (`^[A-Za-z0-9_:.-]{1,150}$`) and IP/CIDR strings are defined centrally in `Validation.java`.

### Rate limiting

Rate limiting is a simple in-memory fixed-window counter (`ConcurrentHashMap<UUID, Window>`). This is intentionally a single-instance design — it does not coordinate across multiple backend replicas. If you run multiple backend instances behind a load balancer, keys can exceed their limit by a factor of the replica count. A production multi-instance deployment would replace this with a Redis-backed sliding window.

### CORS

Allowed origins are read from `CORS_ORIGIN` (comma-separated for multiple). No wildcard is ever used. Credentials are allowed (`allowCredentials: true`) to support future cookie-based auth scenarios without breaking the current JWT flow.

### Error handling and logging

`GlobalExceptionHandler` catches all exceptions. Operational errors (`ApiException` subclasses) return a structured JSON response with a stable `code` string. Unhandled exceptions return a generic `INTERNAL_ERROR` response — no stack traces are ever sent to the client. The full exception is logged server-side.

`RequestLoggingFilter` assigns a UUID `requestId` to every request, puts it in the MDC (so every log line during the request carries it), echoes it as `X-Request-Id` in the response header for correlation, and logs one line per request (method, path, status, duration). Request and response bodies are never logged.

---

## Technology choices

| Layer | Technology | Rationale |
|---|---|---|
| Backend framework | Spring Boot 3.2 | Mature, well-supported, excellent ecosystem for production Java |
| ORM | Spring Data JPA + Hibernate | Standard JPA with Flyway-owned schema (safer than auto-DDL) |
| Migrations | Flyway | Explicit, version-controlled schema history |
| Security | Spring Security 6 (stateless JWT) | Industry standard; no session management overhead |
| JWT | jjwt 0.11.5 | Lightweight, well-maintained Java JWT library |
| Password hashing | BCrypt (Spring Security) | Intentionally slow; appropriate for human passwords |
| Key hashing | SHA-256 (JDK `MessageDigest`) | Fast lookup for high-entropy secrets |
| Frontend | React 18 + TypeScript + Vite | Fast dev experience; type safety; small bundle |
| Data fetching | @tanstack/react-query | Caching, loading states, cache invalidation on mutations |
| Container runtime | Docker + nginx | Multi-stage build for minimal image size; nginx for SPA routing |
