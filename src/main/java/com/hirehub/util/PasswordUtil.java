package com.hirehub.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Utility for hashing and verifying passwords using PBKDF2WithHmacSHA256.
 * Formats hashes as: iterations:saltBase64:hashBase64.
 * Provides constant-time verification to prevent timing attacks.
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int DEFAULT_ITERATIONS = 65_536;
    private static final int SALT_BYTES = 16;
    private static final int KEY_LENGTH_BITS = 256;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /**
     * Hashes a plain-text password using PBKDF2WithHmacSHA256 and a cryptographically secure random salt.
     *
     * @param password plain-text password
     * @return formatted hash string: {@code iterations:saltBase64:hashBase64}
     */
    public static String hashPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        byte[] salt = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, DEFAULT_ITERATIONS, KEY_LENGTH_BITS);

        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hashBase64 = Base64.getEncoder().encodeToString(hash);
        return DEFAULT_ITERATIONS + ":" + saltBase64 + ":" + hashBase64;
    }

    /**
     * Verifies a raw password against the stored password hash.
     * If the stored password is an unhashed legacy string (e.g. initial setup seeds),
     * a constant-time comparison is performed and {@link #needsRehash(String)} will flag it
     * for automatic upgrade upon successful login.
     *
     * @param rawPassword plain-text candidate password
     * @param storedHash  the password hash from the database
     * @return true if candidate password matches stored hash; false otherwise
     */
    public static boolean verifyPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null || rawPassword.isEmpty() || storedHash.isEmpty()) {
            return false;
        }

        String[] parts = storedHash.split(":");
        if (parts.length == 3) {
            try {
                int iterations = Integer.parseInt(parts[0]);
                byte[] salt = Base64.getDecoder().decode(parts[1]);
                byte[] expectedHash = Base64.getDecoder().decode(parts[2]);

                byte[] actualHash = pbkdf2(rawPassword.toCharArray(), salt, iterations, expectedHash.length * 8);
                return MessageDigest.isEqual(actualHash, expectedHash);
            } catch (Exception e) {
                return false;
            }
        }

        // Fallback for unhashed legacy records (e.g. initial schema seeds)
        // Uses constant-time comparison to prevent timing leak
        byte[] rawBytes = rawPassword.getBytes(StandardCharsets.UTF_8);
        byte[] storedBytes = storedHash.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(rawBytes, storedBytes);
    }

    /**
     * Checks if the stored hash should be upgraded to the current PBKDF2 standard and iteration count.
     *
     * @param storedHash password string from database
     * @return true if storedHash is legacy or uses outdated parameters
     */
    public static boolean needsRehash(String storedHash) {
        if (storedHash == null || storedHash.isBlank()) {
            return true;
        }
        String[] parts = storedHash.split(":");
        if (parts.length != 3) {
            return true;
        }
        try {
            int iterations = Integer.parseInt(parts[0]);
            return iterations != DEFAULT_ITERATIONS;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Failed to calculate PBKDF2 hash: " + e.getMessage(), e);
        }
    }
}
