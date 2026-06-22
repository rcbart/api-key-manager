package com.apikeymanager.dto;

import com.apikeymanager.domain.ApiKey;
import java.time.Instant;
import java.util.Set;

/** Everything about a key EXCEPT the raw secret -- safe to return from any
 * "list"/"get" endpoint, repeatedly, forever. The raw key only ever appears
 * once, in {@link CreateApiKeyResponse}, at creation time. */
public class ApiKeySummaryResponse {

    private final String id;
    private final String name;
    private final String displayPrefix;
    private final Set<String> scopes;
    private final Set<String> allowedIps;
    private final Integer rateLimitPerMinute;
    private final Instant expiresAt;
    private final boolean revoked;
    private final Instant revokedAt;
    private final String createdBy;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant lastUsedAt;

    public ApiKeySummaryResponse(
            String id,
            String name,
            String displayPrefix,
            Set<String> scopes,
            Set<String> allowedIps,
            Integer rateLimitPerMinute,
            Instant expiresAt,
            boolean revoked,
            Instant revokedAt,
            String createdBy,
            Instant createdAt,
            Instant updatedAt,
            Instant lastUsedAt) {
        this.id = id;
        this.name = name;
        this.displayPrefix = displayPrefix;
        this.scopes = scopes;
        this.allowedIps = allowedIps;
        this.rateLimitPerMinute = rateLimitPerMinute;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
        this.revokedAt = revokedAt;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastUsedAt = lastUsedAt;
    }

    public static ApiKeySummaryResponse from(ApiKey key) {
        return new ApiKeySummaryResponse(
                key.getId().toString(),
                key.getName(),
                key.getDisplayPrefix(),
                key.getScopes(),
                key.getAllowedIps(),
                key.getRateLimitPerMinute(),
                key.getExpiresAt(),
                key.isRevoked(),
                key.getRevokedAt(),
                key.getCreatedBy(),
                key.getCreatedAt(),
                key.getUpdatedAt(),
                key.getLastUsedAt());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayPrefix() {
        return displayPrefix;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public Set<String> getAllowedIps() {
        return allowedIps;
    }

    public Integer getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }
}
