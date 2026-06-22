package com.apikeymanager.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class CreateApiKeyRequest {

    @NotBlank
    @Size(max = 200)
    private String name;

    @Future
    private Instant expiresAt;

    @Size(max = 50)
    private Set<@Pattern(regexp = Validation.SCOPE_PATTERN) String> scopes = new HashSet<>();

    @Min(1)
    @Max(1_000_000)
    private Integer rateLimitPerMinute;

    @Size(max = 50)
    private Set<@Pattern(regexp = Validation.IP_OR_CIDR_PATTERN) String> allowedIps = new HashSet<>();

    public CreateApiKeyRequest() {
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

    public Set<String> getAllowedIps() {
        return allowedIps;
    }

    public void setAllowedIps(Set<String> allowedIps) {
        this.allowedIps = allowedIps;
    }
}
