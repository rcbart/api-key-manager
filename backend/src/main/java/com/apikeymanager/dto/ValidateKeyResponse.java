package com.apikeymanager.dto;

import java.time.Instant;
import java.util.Set;

public class ValidateKeyResponse {

    private final boolean valid;
    private final String reason;
    private final String name;
    private final Set<String> scopes;
    private final Instant expiresAt;

    private ValidateKeyResponse(boolean valid, String reason, String name, Set<String> scopes, Instant expiresAt) {
        this.valid = valid;
        this.reason = reason;
        this.name = name;
        this.scopes = scopes;
        this.expiresAt = expiresAt;
    }

    public static ValidateKeyResponse valid(String name, Set<String> scopes, Instant expiresAt) {
        return new ValidateKeyResponse(true, null, name, scopes, expiresAt);
    }

    public static ValidateKeyResponse invalid(String reason) {
        return new ValidateKeyResponse(false, reason, null, null, null);
    }

    public boolean isValid() {
        return valid;
    }

    public String getReason() {
        return reason;
    }

    public String getName() {
        return name;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
