package com.apikeymanager.dto;

/** Returned exactly once, immediately after creation. {@code rawKey} is never
 * persisted and can never be retrieved again -- if it's lost, the only
 * remedy is to revoke the key and generate a new one. */
public class CreateApiKeyResponse {

    private final String rawKey;
    private final ApiKeySummaryResponse key;

    public CreateApiKeyResponse(String rawKey, ApiKeySummaryResponse key) {
        this.rawKey = rawKey;
        this.key = key;
    }

    public String getRawKey() {
        return rawKey;
    }

    public ApiKeySummaryResponse getKey() {
        return key;
    }
}
