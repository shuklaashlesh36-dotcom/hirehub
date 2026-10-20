package com.hirehub.util;

import com.hirehub.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InputValidatorTest {

    @Test
    void validateName_trimsValidName() {
        assertEquals("Uma", InputValidator.validateName("  Uma  "));
    }

    @Test
    void validateName_rejectsBlank() {
        assertThrows(ValidationException.class, () -> InputValidator.validateName("  "));
    }

    @Test
    void validateEmail_normalizesCase() {
        assertEquals("user@hirehub.com", InputValidator.validateEmail("User@HireHub.com"));
    }

    @Test
    void validateEmail_rejectsInvalidFormat() {
        assertThrows(ValidationException.class, () -> InputValidator.validateEmail("not-an-email"));
    }

    @Test
    void validatePassword_acceptsMinimumLength() {
        assertEquals("password", InputValidator.validatePassword("password"));
    }

    @Test
    void validatePassword_rejectsShortValue() {
        assertThrows(ValidationException.class, () -> InputValidator.validatePassword("short"));
    }

    @Test
    void requireText_rejectsNull() {
        assertThrows(ValidationException.class, () -> InputValidator.requireText("Title", null));
    }
}
