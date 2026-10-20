package com.hirehub.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void hashPassword_generatesValidHashFormat() {
        String hash = PasswordUtil.hashPassword("SecurePass@123");
        assertNotNull(hash);
        String[] parts = hash.split(":");
        assertEquals(3, parts.length, "Hash must contain iterations:salt:hash");
        assertEquals("65536", parts[0]);
        assertFalse(parts[1].isBlank());
        assertFalse(parts[2].isBlank());
    }

    @Test
    void hashPassword_generatesUniqueSaltsForSamePassword() {
        String hash1 = PasswordUtil.hashPassword("Password@123");
        String hash2 = PasswordUtil.hashPassword("Password@123");

        assertNotEquals(hash1, hash2, "Hashes for the same password must differ due to unique salts.");
        assertTrue(PasswordUtil.verifyPassword("Password@123", hash1));
        assertTrue(PasswordUtil.verifyPassword("Password@123", hash2));
    }

    @Test
    void verifyPassword_withCorrectPassword_returnsTrue() {
        String raw = "StrongP@ssw0rd!";
        String hash = PasswordUtil.hashPassword(raw);

        assertTrue(PasswordUtil.verifyPassword(raw, hash));
    }

    @Test
    void verifyPassword_withIncorrectPassword_returnsFalse() {
        String raw = "StrongP@ssw0rd!";
        String hash = PasswordUtil.hashPassword(raw);

        assertFalse(PasswordUtil.verifyPassword("WrongPassword!", hash));
        assertFalse(PasswordUtil.verifyPassword("strongp@ssw0rd!", hash));
    }

    @Test
    void verifyPassword_withNullOrBlank_returnsFalse() {
        String hash = PasswordUtil.hashPassword("ValidPassword123");

        assertFalse(PasswordUtil.verifyPassword(null, hash));
        assertFalse(PasswordUtil.verifyPassword("", hash));
        assertFalse(PasswordUtil.verifyPassword("ValidPassword123", null));
        assertFalse(PasswordUtil.verifyPassword("ValidPassword123", ""));
    }

    @Test
    void verifyPassword_legacyFallbackAndNeedsRehash() {
        String legacyPlaintext = "ChangeMe@123";

        assertTrue(PasswordUtil.verifyPassword("ChangeMe@123", legacyPlaintext));
        assertFalse(PasswordUtil.verifyPassword("IncorrectPass", legacyPlaintext));
        assertTrue(PasswordUtil.needsRehash(legacyPlaintext));

        String modernHash = PasswordUtil.hashPassword("ChangeMe@123");
        assertFalse(PasswordUtil.needsRehash(modernHash));
    }

    @Test
    void hashPassword_withNullOrBlank_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword("   "));
    }
}
