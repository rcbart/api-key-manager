CREATE TABLE admin_users (
    id            UUID PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE api_keys (
    id                    UUID PRIMARY KEY,
    name                  VARCHAR(200) NOT NULL,
    display_prefix        VARCHAR(64) NOT NULL,
    key_hash              VARCHAR(64) NOT NULL UNIQUE,
    rate_limit_per_minute INTEGER,
    expires_at            TIMESTAMPTZ,
    revoked               BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at            TIMESTAMPTZ,
    created_by            VARCHAR(100) NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_used_at          TIMESTAMPTZ
);

CREATE INDEX idx_api_keys_key_hash ON api_keys (key_hash);
CREATE INDEX idx_api_keys_revoked ON api_keys (revoked);

CREATE TABLE api_key_scopes (
    api_key_id UUID NOT NULL REFERENCES api_keys (id) ON DELETE CASCADE,
    scope      VARCHAR(150) NOT NULL,
    PRIMARY KEY (api_key_id, scope)
);

CREATE TABLE api_key_allowed_ips (
    api_key_id UUID NOT NULL REFERENCES api_keys (id) ON DELETE CASCADE,
    ip_address VARCHAR(64) NOT NULL,
    PRIMARY KEY (api_key_id, ip_address)
);
