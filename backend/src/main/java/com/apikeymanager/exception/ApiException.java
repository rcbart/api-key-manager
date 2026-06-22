package com.apikeymanager.exception;

import org.springframework.http.HttpStatus;

/** Base type for operational errors we throw deliberately, with an HTTP
 * status and a stable machine-readable code attached. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(String message, HttpStatus status, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
