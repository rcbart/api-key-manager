package com.apikeymanager.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;

public class ApiKeyNotFoundException extends ApiException {

    public ApiKeyNotFoundException(UUID id) {
        super("API key not found: " + id, HttpStatus.NOT_FOUND, "API_KEY_NOT_FOUND");
    }
}
