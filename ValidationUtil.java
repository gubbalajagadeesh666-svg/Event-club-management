package com.campusflow.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Server-side input normalization and validation helpers.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,24}$"
    );

    private ValidationUtil() {
    }

    public static String trimOrNull(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.length() > 150 || email.length() < 5) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static Integer parsePositiveInt(String raw) {
        String val = trimOrNull(raw);
        if (val == null) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(val);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static LocalDate parseDate(String raw) {
        String val = trimOrNull(raw);
        if (val == null) {
            return null;
        }
        try {
            return LocalDate.parse(val);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    public static LocalTime parseTime(String raw) {
        String val = trimOrNull(raw);
        if (val == null) {
            return null;
        }
        try {
            return LocalTime.parse(val);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    public static LocalDateTime parseDateTimeLocal(String raw) {
        String val = trimOrNull(raw);
        if (val == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(val);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    public static String slugify(String text) {
        if (text == null) {
            return "club";
        }
        String slug = text.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (slug.isEmpty()) {
            slug = "club-" + System.currentTimeMillis();
        }
        return slug.length() > 48 ? slug.substring(0, 48) : slug;
    }
}
