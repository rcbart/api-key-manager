package com.apikeymanager.exception;

import org.springframework.http.HttpStatus;

/** Thrown for a failed admin login. Deliberately generic ("invalid username or
 * password") so the API never reveals whether a username exists. */
public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        super("Invalid username or password", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }
}
