# API Reference

Base URL: `http://<host>:<port>`

All admin endpoints require a JWT in the `Authorization: Bearer <token>` header obtained from the login endpoint.

---

## Authentication

### POST /api/admin/auth/login

Authenticate as an admin and receive a JWT.

**Request body:**
```json
{
  "username": "admin",
  "password": "your_password"
}
```

**Response 200:**
```json
{
  "token": "eyJhbGci...",
  "expiresAt": "2026-06-23T12:00:00Z",
  "username": "admin"
}
```

**Response 401:**
```json
{
  "code": "INVALID_CREDENTIALS",
  "message": "Invalid username or password",
  "timestamp": "2026-06-22T10:00:00Z"
}
```

---

## API Key Management (admin only)

### GET /api/admin/keys

List all API keys (raw key never returned).

**Response 200:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "CI pipeline",
    "displayPrefix": "ak_live_Ab1Cd2",
    "scopes": ["read:orders"],
    "allowedIps": ["10.0.0.0/8"],
    "rateLimitPerMinute": 60,
    "expiresAt": "2026-12-31T23:59:59Z",
    "revoked": false,
    "revokedAt": null,
    "createdBy": "admin",
    "createdAt": "2026-06-22T09:00:00Z",
    "updatedAt": "2026-06-22T09:00:00Z",
    "lastUsedAt": "2026-06-22T11:30:00Z"
  }
]
```

### POST /api/admin/keys

Generate a new API key.

**Request body:**
```json
{
  "name": "CI pipeline",
  "expiresAt": "2026-12-31T23:59:59Z",
  "scopes": ["read:orders", "write:orders"],
  "rateLimitPerMinute": 60,
  "allowedIps": ["10.0.0.0/8", "203.0.113.5"]
}
```

All fields except `name` are optional.

**Response 200:**
```json
{
  "rawKey": "ak_live_Ab1Cd2EfGhIjKlMnOpQrStUvWxYz01234567",
  "key": { ...ApiKeySummary... }
}
```

> **Save the `rawKey` now.** It is returned exactly once and never stored.

### PATCH /api/admin/keys/{id}

Update a key's metadata (name, scopes, rate limit, IP allowlist). The raw key itself cannot change.

**Request body (all fields optional — only provided fields are updated):**
```json
{
  "name": "CI pipeline v2",
  "scopes": ["read:orders"],
  "rateLimitPerMinute": 120,
  "clearRateLimit": false,
  "allowedIps": []
}
```

Set `clearRateLimit: true` (and omit `rateLimitPerMinute`) to remove a rate limit.

**Response 200:** Updated `ApiKeySummary` object.

### POST /api/admin/keys/{id}/revoke

Immediately and permanently revoke a key. Subsequent validation checks will return `reason: "revoked"`.

**Response 200:** Updated `ApiKeySummary` object with `revoked: true`.

### DELETE /api/admin/keys/{id}

Permanently delete a key and all its metadata.

**Response 204:** No content.

---

## Validation (public)

### POST /api/validate

Check whether a raw API key is valid for a given context. Intended for use by your own backend services.

**Headers:**
- `X-API-Key: <raw key>` — required (the key to validate)

**Request body (optional):**
```json
{
  "sourceIp": "203.0.113.42",
  "requiredScope": "read:orders"
}
```

**Response 200 (always):**

Valid:
```json
{
  "valid": true,
  "keyName": "CI pipeline",
  "scopes": ["read:orders"],
  "expiresAt": "2026-12-31T23:59:59Z"
}
```

Invalid:
```json
{
  "valid": false,
  "reason": "rate_limited"
}
```

**Reason values:** `missing_key`, `invalid_key`, `revoked`, `expired`, `rate_limited`, `ip_not_allowed`, `insufficient_scope`

---

## Error responses

All endpoints return errors in this shape:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": { "name": "must not be blank" },
  "timestamp": "2026-06-22T10:00:00Z"
}
```

| HTTP status | Common `code` values |
|---|---|
| 400 | `VALIDATION_ERROR`, `MALFORMED_REQUEST` |
| 401 | `UNAUTHORIZED`, `INVALID_CREDENTIALS` |
| 403 | `FORBIDDEN` |
| 404 | `API_KEY_NOT_FOUND` |
| 500 | `INTERNAL_ERROR` |

---

## Health check

### GET /actuator/health

Returns application health. No authentication required.

**Response 200:**
```json
{ "status": "UP" }
```
