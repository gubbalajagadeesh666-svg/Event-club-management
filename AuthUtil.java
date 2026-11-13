package com.campusflow.util;

import com.campusflow.model.User;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Server-side session authentication and role-checking helper.
 * Stores only minimal non-sensitive user attributes in HttpSession.
 */
public final class AuthUtil {

    public static final String ATTR_USER_ID = "authUserId";
    public static final String ATTR_USER_NAME = "authUserName";
    public static final String ATTR_USER_EMAIL = "authUserEmail";
    public static final String ATTR_USER_ROLE = "authUserRole";

    private AuthUtil() {
    }

    /**
     * Establishes an authenticated session, regenerating the session ID via Servlet 4.0
     * request.changeSessionId() to prevent session fixation attacks.
     */
    public static HttpSession establishAuthenticatedSession(HttpServletRequest request, User user) {
        HttpSession existing = request.getSession(false);
        if (existing != null) {
            existing.removeAttribute(ATTR_USER_ID);
            existing.removeAttribute(ATTR_USER_NAME);
            existing.removeAttribute(ATTR_USER_EMAIL);
            existing.removeAttribute(ATTR_USER_ROLE);
        }
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setMaxInactiveInterval(20 * 60); // 20 minutes inactivity timeout
        session.setAttribute(ATTR_USER_ID, user.getUserId());
        session.setAttribute(ATTR_USER_NAME, user.getFullName());
        session.setAttribute(ATTR_USER_EMAIL, user.getEmail());
        session.setAttribute(ATTR_USER_ROLE, user.getRole());
        CsrfUtil.rotateToken(session);
        return session;
    }

    public static boolean isAuthenticated(HttpServletRequest request) {
        return getUserId(request) != null && getUserRole(request) != null;
    }

    public static Integer getUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object id = session.getAttribute(ATTR_USER_ID);
        return (id instanceof Integer val) ? val : null;
    }

    public static String getUserName(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object name = session.getAttribute(ATTR_USER_NAME);
        return (name instanceof String val) ? val : null;
    }

    public static String getUserRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object role = session.getAttribute(ATTR_USER_ROLE);
        return (role instanceof String val) ? val : null;
    }

    public static boolean hasRole(HttpServletRequest request, String... allowedRoles) {
        String currentRole = getUserRole(request);
        if (currentRole == null) {
            return false;
        }
        for (String role : allowedRoles) {
            if (currentRole.equals(role)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAdmin(HttpServletRequest request) {
        return User.ROLE_ADMIN.equals(getUserRole(request));
    }
}
