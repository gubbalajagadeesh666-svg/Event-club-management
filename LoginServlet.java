package com.campusflow.servlet;

import com.campusflow.dao.UserDAO;
import com.campusflow.model.User;
import com.campusflow.util.AuthUtil;
import com.campusflow.util.FlashUtil;
import com.campusflow.util.PasswordUtil;
import com.campusflow.util.ValidationUtil;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User authentication controller (/login).
 * Verifies BCrypt password hashes, enforces ACTIVE status, regenerates session ID
 * on login to prevent session fixation, and avoids revealing which credential field failed.
 */
public class LoginServlet extends HttpServlet {

    // Pre-computed dummy BCrypt hash to equalize timing when email does not exist
    private static final String DUMMY_BCRYPT_HASH =
            "$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW";

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (AuthUtil.isAuthenticated(req)) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        if ("1".equals(req.getParameter("signedOut"))) {
            req.setAttribute("infoBanner", "You have been signed out safely.");
        } else if ("1".equals(req.getParameter("expired"))) {
            req.setAttribute("warningBanner", "Session expired; please log in again.");
        } else if ("1".equals(req.getParameter("authRequired"))) {
            req.setAttribute("warningBanner", "Please log in to access that page.");
        } else if ("1".equals(req.getParameter("inactive"))) {
            req.setAttribute("formError", "Your account is currently inactive. Please contact an administrator.");
        }

        try {
            req.setAttribute("needsInitialSetup", userDAO.countAdmins() == 0);
        } catch (SQLException ex) {
            getServletContext().log("Database connectivity warning on LoginServlet", ex);
            req.setAttribute("dbWarning", "Unable to connect to the database. Please ensure MySQL is running.");
        }

        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = ValidationUtil.normalizeEmail(req.getParameter("email"));
        String password = req.getParameter("password");

        req.setAttribute("submittedEmail", email);

        if (email == null || password == null || password.isEmpty()) {
            req.setAttribute("formError", "Please enter both your email address and password.");
            populateSetupFlagQuietly(req);
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        try {
            User user = userDAO.findByEmailForAuth(email);
            if (user == null) {
                PasswordUtil.verifyPassword(password, DUMMY_BCRYPT_HASH);
                req.setAttribute("formError", "Invalid login credentials.");
                populateSetupFlagQuietly(req);
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
                return;
            }

            if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                req.setAttribute("formError", "Invalid login credentials.");
                populateSetupFlagQuietly(req);
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
                return;
            }

            if (!user.isActive()) {
                req.setAttribute("formError", "Your account is inactive. Please contact the college administrator.");
                populateSetupFlagQuietly(req);
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
                return;
            }

            AuthUtil.establishAuthenticatedSession(req, user);
            FlashUtil.setSuccess(req, "Welcome back, " + user.getFullName() + "!");
            resp.sendRedirect(req.getContextPath() + "/dashboard");
        } catch (SQLException ex) {
            getServletContext().log("Database error during login", ex);
            req.setAttribute("formError", "Unable to connect to the database. Please try again shortly.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }

    private void populateSetupFlagQuietly(HttpServletRequest req) {
        try {
            req.setAttribute("needsInitialSetup", userDAO.countAdmins() == 0);
        } catch (SQLException ignored) {
        }
    }
}
