package com.campusflow.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Synchronizer-token CSRF protection for state-changing POST requests.
 */
public final class CsrfUtil {

    public static final String SESSION_CSRF_KEY = "csrfToken";
    public static final String FORM_CSRF_PARAM = "_csrf";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CsrfUtil() {
    }

    public static String ensureToken(HttpSession session) {
        Object existing = session.getAttribute(SESSION_CSRF_KEY);
        if (existing instanceof String token && !token.isBlank()) {
            return token;
        }
        String newToken = generateToken();
        session.setAttribute(SESSION_CSRF_KEY, newToken);
        return newToken;
    }

    public static String rotateToken(HttpSession session) {
        String newToken = generateToken();
        session.setAttribute(SESSION_CSRF_KEY, newToken);
        return newToken;
    }

    public static boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object expectedObj = session.getAttribute(SESSION_CSRF_KEY);
        String submitted = request.getParameter(FORM_CSRF_PARAM);
        if (!(expectedObj instanceof String expected) || submitted == null || submitted.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                submitted.trim().getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
