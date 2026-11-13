package com.campusflow.filter;

import com.campusflow.dao.UserDAO;
import com.campusflow.model.User;
import com.campusflow.util.AuthUtil;
import com.campusflow.util.CsrfUtil;
import com.campusflow.util.FlashUtil;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Central UTF-8 encoding, cache-control, authentication, session verification,
 * CSRF validation, and URL-level role authorization filter.
 *
 * Configured in WEB-INF/web.xml for DispatcherType.REQUEST only so internal
 * RequestDispatcher.forward() calls to JSP views do not re-trigger redirects.
 */
public class AuthFilter implements Filter {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("X-Content-Type-Options", "nosniff");

        String contextPath = req.getContextPath();
        String uri = req.getRequestURI();
        String path = uri.substring(contextPath.length());
        if (path.isEmpty()) {
            path = "/";
        }

        // Static assets pass straight through
        if (path.startsWith("/assets/") || path.equals("/favicon.ico")) {
            chain.doFilter(req, resp);
            return;
        }

        // Route direct public JSP hits through their Servlets so zero SQL lives in JSPs
        if (path.equals("/") || path.equals("/index.jsp")) {
            resp.sendRedirect(contextPath + "/home");
            return;
        }
        if (path.equals("/login.jsp")) {
            resp.sendRedirect(contextPath + "/login");
            return;
        }
        if (path.equals("/register.jsp")) {
            resp.sendRedirect(contextPath + "/register");
            return;
        }
        if (path.equals("/setup.jsp")) {
            resp.sendRedirect(contextPath + "/setup");
            return;
        }

        // Public Servlet routes
        if (path.equals("/home") || path.equals("/login") || path.equals("/register")
                || path.equals("/setup") || path.equals("/error.jsp")) {
            FlashUtil.consumeToRequest(req);
            chain.doFilter(req, resp);
            return;
        }

        // All remaining routes are protected: apply strict anti-cache headers (Section 8.4)
        applyNoCacheHeaders(resp);

        Integer userId = AuthUtil.getUserId(req);
        if (userId == null) {
            // Distinguish expired cookie session from fresh unauthenticated request
            boolean hadRequestedSession = req.getRequestedSessionId() != null && !req.isRequestedSessionIdValid();
            String query = hadRequestedSession ? "?expired=1" : "?authRequired=1";
            resp.sendRedirect(contextPath + "/login" + query);
            return;
        }

        // Verify account still exists and is ACTIVE in MySQL, and sync authoritative role
        User currentUser;
        try {
            currentUser = userDAO.findById(userId);
        } catch (SQLException ex) {
            req.setAttribute("errorTitle", "Database Unavailable");
            req.setAttribute("errorMessage", "Unable to connect to the database. Please try again shortly.");
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
            return;
        }

        HttpSession session = req.getSession(false);
        if (currentUser == null || !currentUser.isActive()) {
            if (session != null) {
                session.invalidate();
            }
            resp.sendRedirect(contextPath + "/login?inactive=1");
            return;
        }

        // Synchronize live role & display name from database
        if (session != null) {
            session.setAttribute(AuthUtil.ATTR_USER_ROLE, currentUser.getRole());
            session.setAttribute(AuthUtil.ATTR_USER_NAME, currentUser.getFullName());
            CsrfUtil.ensureToken(session);
        }
        req.setAttribute("currentUser", currentUser);

        // Enforce role boundaries BEFORE routing direct JSP or Servlet requests
        String role = currentUser.getRole();
        if (path.startsWith("/admin") || path.equals("/admin-users.jsp")) {
            if (!User.ROLE_ADMIN.equals(role)) {
                denyAccess(req, resp);
                return;
            }
        }
        if (path.startsWith("/reports") || path.equals("/reports.jsp")) {
            if (User.ROLE_STUDENT.equals(role)) {
                denyAccess(req, resp);
                return;
            }
        }
        if (path.startsWith("/attendance") || path.equals("/attendance.jsp")) {
            if (User.ROLE_STUDENT.equals(role)) {
                denyAccess(req, resp);
                return;
            }
        }

        // Redirect direct hits on protected JSP templates to their controller Servlets
        if (path.endsWith(".jsp")) {
            switch (path) {
                case "/dashboard.jsp" -> resp.sendRedirect(contextPath + "/dashboard");
                case "/clubs.jsp", "/club-details.jsp" -> resp.sendRedirect(contextPath + "/clubs");
                case "/events.jsp", "/event-details.jsp" -> resp.sendRedirect(contextPath + "/events");
                case "/lakshya.jsp" -> resp.sendRedirect(contextPath + "/lakshya");
                case "/my-registrations.jsp" -> resp.sendRedirect(contextPath + "/registrations");
                case "/attendance.jsp" -> resp.sendRedirect(contextPath + "/events");
                case "/reports.jsp" -> resp.sendRedirect(contextPath + "/reports");
                case "/admin-users.jsp" -> resp.sendRedirect(contextPath + "/admin/users");
                default -> resp.sendRedirect(contextPath + "/dashboard");
            }
            return;
        }

        // Enforce CSRF token on all authenticated state-changing POST requests
        if ("POST".equalsIgnoreCase(req.getMethod())) {
            if (!CsrfUtil.isValid(req)) {
                FlashUtil.setError(req, "Security verification failed (invalid or expired form token). Please try again.");
                resp.sendRedirect(contextPath + "/dashboard");
                return;
            }
        }

        FlashUtil.consumeToRequest(req);
        chain.doFilter(req, resp);
    }

    private void applyNoCacheHeaders(HttpServletResponse resp) {
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);
    }

    private void denyAccess(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        req.setAttribute("errorTitle", "403 — Access Denied");
        req.setAttribute("errorMessage", "You do not have permission to perform this action.");
        req.getRequestDispatcher("/error.jsp").forward(req, resp);
    }

    @Override
    public void destroy() {
    }
}
