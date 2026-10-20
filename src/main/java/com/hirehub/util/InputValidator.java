package com.hirehub.util;

import com.hirehub.exception.ValidationException;

/**
 * Shared field checks for forms. Keep rules simple and reuse them from services later.
 */
public final class InputValidator {

    private static final int MIN_NAME_LENGTH = 2;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private InputValidator() {
    }

    public static String requireText(String fieldName, String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required.");
        }
        return value.trim();
    }

    public static String validateName(String name) {
        String trimmed = requireText("Name", name);
        if (trimmed.length() < MIN_NAME_LENGTH) {
            throw new ValidationException("Name must be at least " + MIN_NAME_LENGTH + " characters.");
        }
        return trimmed;
    }

    public static String validateEmail(String email) {
        String trimmed = requireText("Email", email).toLowerCase();
        if (!trimmed.matches(EMAIL_PATTERN)) {
            throw new ValidationException("Email format is invalid.");
        }
        return trimmed;
    }

    public static String validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ValidationException("Password is required.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        return password;
    }
}
