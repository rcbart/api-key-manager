package com.apikeymanager.dto;

import jakarta.validation.constraints.Pattern;

/**
 * Optional extra context for a validation check. The key itself is read
 * from the {@code X-API-Key} header (see ValidationController), not the
 * body -- that keeps it out of any logging middleware that might record
 * request bodies, and matches how most API-key systems expect to be called.
 */
public class ValidateKeyRequest {

    @Pattern(regexp = Validation.IP_OR_CIDR_PATTERN)
    private String sourceIp;

    @Pattern(regexp = Validation.SCOPE_PATTERN)
    private String requiredScope;

    public ValidateKeyRequest() {
        // required for JSON deserialization
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getRequiredScope() {
        return requiredScope;
    }

    public void setRequiredScope(String requiredScope) {
        this.requiredScope = requiredScope;
    }
}
