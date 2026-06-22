# User Guide

## Logging in

Open the app URL in your browser. You will see a login screen. Enter your admin username and password, then click **Sign in**.

Your session is stored in `sessionStorage` and expires automatically when the browser tab is closed or the JWT (default: 12 hours) expires. Click **Sign out** at any time to end the session immediately.

---

## Generating an API key

1. On the dashboard, fill in the **Generate new key** form at the top.
2. **Name** is required — give the key a descriptive label (e.g. "CI pipeline", "Partner integration").
3. Optionally configure:
   - **Expiration** — pick a date/time; leave blank for a key that never expires.
   - **Scopes** — type a scope string (e.g. `read:orders`) and click **Add** or press Enter. Add as many as needed. Leave empty for unrestricted access to any scope.
   - **Rate limit** — maximum number of validation requests per minute. Leave blank for unlimited.
   - **Allowed IPs / CIDR blocks** — type an IPv4 address, IPv6 address, or CIDR range (e.g. `10.0.0.0/8`, `203.0.113.5`, `2001:db8::/32`) and click **Add**. Leave empty to allow requests from any source IP.
4. Click **Generate key**.
5. A dialog shows the full raw key — **copy it now**. This is the only time it will be displayed. Click the copy icon to copy to clipboard, confirm you have saved it, and click **Done**.

---

## Managing existing keys

The key table below the form lists all keys with their current status.

**Status meanings:**
- **Active** — key is valid and will pass validation checks.
- **Expired** — key's expiration date has passed; all validation checks will fail.
- **Revoked** — key has been explicitly revoked; all validation checks will fail immediately.

**Actions per key:**
- **Edit** — update the key's name, scopes, rate limit, or IP allowlist. The raw key value itself cannot be changed.
- **Revoke** — immediately invalidate the key. Revocation is permanent and cannot be undone from the UI. Delete the key if you no longer need it in the list.
- **Delete** — permanently remove the key and its metadata from the database.

---

## Validating keys (downstream services)

Other services call `POST /api/validate` to check whether a key is permitted for a given request. This endpoint is public (no admin JWT required) and is designed to be called by your own backend code.

### Request

```http
POST /api/validate
X-API-Key: ak_live_<the raw key>
Content-Type: application/json

{
  "sourceIp": "203.0.113.42",
  "requiredScope": "read:orders"
}
```

The request body is optional. `sourceIp` and `requiredScope` may be omitted independently:
- If `sourceIp` is omitted, the IP of the caller itself is used for allowlist checking.
- If `requiredScope` is omitted, scope checking is skipped.

### Response (always HTTP 200)

**Valid key:**
```json
{
  "valid": true,
  "keyName": "CI pipeline",
  "scopes": ["read:orders", "write:orders"],
  "expiresAt": "2026-12-31T23:59:59Z"
}
```

**Invalid key:**
```json
{
  "valid": false,
  "reason": "rate_limited"
}
```

**Reason values:**

| reason | Meaning |
|---|---|
| `missing_key` | No `X-API-Key` header was sent |
| `invalid_key` | Key not found in the database |
| `revoked` | Key has been revoked |
| `expired` | Key's expiration date has passed |
| `rate_limited` | Key has exceeded its per-minute rate limit |
| `ip_not_allowed` | Caller's IP is not on the key's allowlist |
| `insufficient_scope` | Key does not have the required scope |
