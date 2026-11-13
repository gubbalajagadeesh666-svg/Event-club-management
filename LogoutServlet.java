package com.campusflow.servlet;

import com.campusflow.util.FlashUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Logout controller (/logout).
 * Enforces POST-only state change (CSRF-verified by AuthFilter), invalidates HttpSession,
 * clears cache headers, and redirects to /login?signedOut=1.
 */
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Section 8.4: Do not allow state-changing logout via GET
        FlashUtil.setWarning(req, "Please use the Sign Out button in the navigation bar to log out securely.");
        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);
        resp.sendRedirect(req.getContextPath() + "/login?signedOut=1");
    }
}
