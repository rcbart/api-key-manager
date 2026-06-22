# API Key Manager

A self-hosted API key lifecycle manager: generate, validate, revoke, and delete keys through a React admin UI backed by a Spring Boot / PostgreSQL service.

**Version:** 0.1.0 | **License:** MIT

---

## Features

- Generate cryptographically secure API keys (256-bit SecureRandom, URL-safe Base64)
- Customise each key: name/label, expiration date, scope/permission list, rate limit (per minute), IP/CIDR allowlist
- Revoke keys immediately; revoked keys fail validation at next check
- Validate keys via a lightweight `POST /api/validate` endpoint (intended for downstream services)
- Admin UI secured with username/password login and short-lived JWTs
- Rate limiting in-memory (single-instance), IP/CIDR matching for both IPv4 and IPv6

## Quick start (Docker Compose)

```bash
cp .env.example .env          # fill in all values
docker compose up --build -d
open http://localhost          # default admin UI port
```

See [docs/DOCKER.md](docs/DOCKER.md) for a complete step-by-step container guide.

## Local development

See [docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md).

## Documentation

| Document | Contents |
|---|---|
| [SETUP_GUIDE.md](docs/SETUP_GUIDE.md) | Prerequisites, local dev setup, running tests |
| [USER_GUIDE.md](docs/USER_GUIDE.md) | How to use the admin UI and validation endpoint |
| [API.md](docs/API.md) | Full REST API reference |
| [DOCKER.md](docs/DOCKER.md) | Container setup and deployment |
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | System design and component overview |
| [SECURITY.md](docs/SECURITY.md) | Security model, threat model, and hardening notes |
| [GITHUB_RELEASE.md](docs/GITHUB_RELEASE.md) | Step-by-step guide to push to GitHub and cut a release |

## License

MIT — see [LICENSE](LICENSE).
