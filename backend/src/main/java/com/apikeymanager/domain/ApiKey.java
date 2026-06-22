package com.apikeymanager.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * An API key's metadata. The raw key value itself is never stored -- only
 * {@link #keyHash}, a SHA-256 digest -- and is shown to the creator exactly
 * once, at creation time. See docs/SECURITY.md for why SHA-256 (not bcrypt)
 * is the right hash here: unlike a user password, a generated key already
 * has 256 bits of entropy, so a fast, deterministic, lookup-friendly hash is
 * appropriate, and bcrypt's deliberate slowness would only hurt validation
 * throughput without adding real security.
 */
@Entity
@Table(name = "api_keys")
public class ApiKey {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "display_prefix", nullable = false, length = 64)
    private String displayPrefix;

    @Column(name = "key_hash", nullable = false, unique = true, length = 64)
    private String keyHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "api_key_scopes", joinColumns = @JoinColumn(name = "api_key_id"))
    @Column(name = "scope")
    private Set<String> scopes = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "api_key_allowed_ips", joinColumns = @JoinColumn(name = "api_key_id"))
    @Column(name = "ip_address")
    private Set<String> allowedIps = new HashSet<>();

    @Column(name = "rate_limit_per_minute")
    private Integer rateLimitPerMinute;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    protected ApiKey() {
        // required by JPA
    }

    public ApiKey(
            UUID id,
            String name,
            String displayPrefix,
            String keyHash,
            Set<String> scopes,
            Set<String> allowedIps,
            Integer rateLimitPerMinute,
            Instant expiresAt,
            String createdBy,
            Instant createdAt) {
        this.id = id;
        this.name = name;
        this.displayPrefix = displayPrefix;
        this.keyHash = keyHash;
        this.scopes = scopes != null ? new HashSet<>(scopes) : new HashSet<>();
        this.allowedIps = allowedIps != null ? new HashSet<>(allowedIps) : new HashSet<>();
        this.rateLimitPerMinute = rateLimitPerMinute;
        this.expiresAt = expiresAt;
        this.revoked = false;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public boolean isExpired(Instant now) {
        return expiresAt != null && expiresAt.isBefore(now);
    }

    public void revoke(Instant when) {
        this.revoked = true;
        this.revokedAt = when;
        this.updatedAt = when;
    }

    public void touchLastUsed(Instant when) {
        this.lastUsedAt = when;
    }

    public void applyUpdate(
            String name,
            Set<String> scopes,
            Set<String> allowedIps,
            Integer rateLimitPerMinute,
            Instant expiresAt,
            Instant when) {
        if (name != null) {
            this.name = name;
        }
        if (scopes != null) {
            this.scopes = new HashSet<>(scopes);
        }
        if (allowedIps != null) {
            this.allowedIps = new HashSet<>(allowedIps);
        }
        this.rateLimitPerMinute = rateLimitPerMinute;
        this.expiresAt = expiresAt;
        this.updatedAt = when;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayPrefix() {
        return displayPrefix;
    }

    public String getKeyHash() {
        return keyHash;
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
