package com.apikeymanager.web;

import java.time.Instant;
import java.util.Map;

public class ErrorResponse {

    private final String code;
    private final String message;
    private final Map<String, String> details;
    private final Instant timestamp;

    public ErrorResponse(String code, String message, Map<String, String> details, Instant timestamp) {
        this.code = code;
        this.message = message;
        this.details = details;
        this.timestamp = timestamp;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
