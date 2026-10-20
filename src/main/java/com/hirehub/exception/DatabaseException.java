package com.hirehub.exception;

import java.sql.SQLException;

/**
 * Unchecked wrapper for JDBC failures. Servlets and tests can catch this
 * without leaking SQLException details into higher layers.
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
