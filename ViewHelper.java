package com.campusflow.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Presentation-layer output escaping and formatting helper for JSP pages.
 * Ensures all dynamic values rendered in HTML or attributes are properly encoded against XSS.
 */
public final class ViewHelper {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DATETIME_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter INPUT_DATETIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter INPUT_TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    private ViewHelper() {
    }

    /**
     * Escapes HTML special characters to prevent Cross-Site Scripting (XSS).
     */
    public static String esc(Object value) {
        if (value == null) {
            return "";
        }
        String str = value.toString();
        StringBuilder out = new StringBuilder(str.length() + 16);
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&#39;");
                default -> out.append(c);
            }
        }
        return out.toString();
    }

    public static String formatDate(LocalDate date, String emptyFallback) {
        return date == null ? emptyFallback : date.format(DATE_FMT);
    }

    public static String formatTime(LocalTime time, String emptyFallback) {
        return time == null ? emptyFallback : time.format(TIME_FMT);
    }

    public static String formatDateTime(LocalDateTime dt, String emptyFallback) {
        return dt == null ? emptyFallback : dt.format(DATETIME_FMT);
    }

    public static String toInputDate(LocalDate date) {
        return date == null ? "" : date.toString();
    }

    public static String toInputTime(LocalTime time) {
        return time == null ? "" : time.format(INPUT_TIME_FMT);
    }

    public static String toInputDateTime(LocalDateTime dt) {
        return dt == null ? "" : dt.format(INPUT_DATETIME_FMT);
    }

    public static String csrf(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        return esc(CsrfUtil.ensureToken(session));
    }

    public static String roleDisplay(String role) {
        if (role == null) {
            return "Student";
        }
        return switch (role) {
            case "ADMIN" -> "Administrator";
            case "FACULTY_COORDINATOR" -> "Faculty Coordinator";
            case "CLUB_LEADER" -> "Club Leader";
            default -> "Student";
        };
    }
}
