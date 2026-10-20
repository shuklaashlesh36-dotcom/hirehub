package com.hirehub.exception;

/**
 * Thrown when an authentication attempt fails (e.g. invalid credentials or deactivated user).
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
