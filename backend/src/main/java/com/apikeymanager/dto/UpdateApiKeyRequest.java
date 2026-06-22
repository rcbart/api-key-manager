package com.apikeymanager.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;

/** All fields optional -- only non-null fields are applied. To clear
 * expiresAt/rateLimitPerMinute entirely, send the dedicated "clear" flags. */
public class UpdateApiKeyRequest {

    @Size(max = 200)
    private String name;

    @Future
    private Instant expiresAt;

    private boolean clearExpiresAt;

    @Size(max = 50)
    private Set<@Pattern(regexp = Validation.SCOPE_PATTERN) String> scopes;

    @Min(1)
    @Max(1_000_000)
    private Integer rateLimitPerMinute;

    private boolean clearRateLimit;

    @Size(max = 50)
    private Set<@Pattern(regexp = Validation.IP_OR_CIDR_PATTERN) String> allowedIps;

    public UpdateApiKeyRequest() {
        // required for JSON deserialization
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isClearExpiresAt() {
        return clearExpiresAt;
    }

    public void setClearExpiresAt(boolean clearExpiresAt) {
        this.clearExpiresAt = clearExpiresAt;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public Integer getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    public void setRateLimitPerMinute(Integer rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
    }

    public boolean isClearRateLimit() {
        return clearRateLimit;
    }

    public void setClearRateLimit(boolean clearRateLimit) {
        this.clearRateLimit = clearRateLimit;
    }

    public Set<String> getAllowedIps() {
        return allowedIps;
    }

    public void setAllowedIps(Set<String> allowedIps) {
        this.allowedIps = allowedIps;
    }
}
