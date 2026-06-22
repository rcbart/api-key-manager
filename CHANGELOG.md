# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.1.0] — 2026-06-22

### Added

- API key generation using 256-bit `SecureRandom` + URL-safe Base64 encoding
- Configurable key parameters: name/label, expiration date, scopes/permissions, per-minute rate limit, IP/CIDR allowlist
- SHA-256 key hashing — raw key stored only in the creation response, never in the database
- Key revocation (immediate; permanent) and deletion
- `POST /api/validate` public endpoint for downstream services to validate keys (checks revocation, expiry, rate limit, IP allowlist, scope)
- In-memory fixed-window rate limiter per key
- IPv4 and IPv6 CIDR bitmask IP matching
- Admin UI (React 18 + TypeScript + Vite) with login, key table, create/edit/revoke/delete actions
- Admin authentication via username/password + short-lived JWT (HS256)
- Timing-attack-resistant login (dummy BCrypt comparison for unknown usernames)
- Spring Boot 3.2 backend with Spring Data JPA, Flyway migrations, PostgreSQL
- Jakarta Bean Validation on all controller inputs with custom scope and IP regex patterns
- Structured JSON error responses — no stack traces exposed to clients
- MDC-based request ID logging; every request correlated by `X-Request-Id`
- Docker multi-stage build for both backend (JDK → JRE) and frontend (Node → nginx)
- `docker-compose.yml` with PostgreSQL health check and service dependency ordering
- GitHub Actions CI workflow (backend Maven verify + frontend build + frontend tests)
- Full documentation: SETUP\_GUIDE, USER\_GUIDE, API reference, ARCHITECTURE, SECURITY, DOCKER, GITHUB\_RELEASE guide
- MIT License

[0.1.0]: https://github.com/<your-username>/api-key-manager/releases/tag/v0.1.0
