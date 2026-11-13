package com.campusflow.util;

import java.nio.charset.StandardCharsets;
import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt password hashing and password policy verification utility.
 */
public final class PasswordUtil {

    private static final int BCRYPT_WORK_FACTOR = 12;
    private static final int MIN_LENGTH = 8;
    private static final int MAX_BYTES = 72; // BCrypt input byte limit

    private PasswordUtil() {
    }

    /**
     * Validates password strength policy.
     * Returns null if valid, or a user-friendly validation message if invalid.
     */
    public static String validatePasswordPolicy(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return "Password must be at least " + MIN_LENGTH + " characters long.";
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            return "Password must not exceed 72 characters.";
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        if (!hasUpper || !hasLower || !hasDigit) {
            return "Password must include at least one uppercase letter, one lowercase letter, and one number.";
        }
        return null;
    }

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_WORK_FACTOR));
    }

    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || !storedHash.startsWith("$2")) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
