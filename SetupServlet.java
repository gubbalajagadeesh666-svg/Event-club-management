package com.campusflow.servlet;

import com.campusflow.dao.UserDAO;
import com.campusflow.model.User;
import com.campusflow.util.CsrfUtil;
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
 * Controlled first-run setup wizard (/setup) for creating the initial Administrator account.
 * Automatically disables itself permanently as soon as at least one ADMIN exists in MySQL.
 */
public class SetupServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            if (userDAO.countAdmins() > 0) {
                FlashUtil.setInfo(req, "Initial administrator setup is already complete. Please sign in.");
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }
        } catch (SQLException ex) {
            getServletContext().log("Database error checking initial setup state", ex);
            req.setAttribute("formError", "Unable to connect to the database. Please verify MySQL and campusflow_db.");
        }
        CsrfUtil.ensureToken(req.getSession(true));
        req.getRequestDispatcher("/setup.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!CsrfUtil.isValid(req)) {
            req.setAttribute("formError", "Invalid security token. Please reload the page and try again.");
            CsrfUtil.ensureToken(req.getSession(true));
            req.getRequestDispatcher("/setup.jsp").forward(req, resp);
            return;
        }

        String fullName = ValidationUtil.trimOrNull(req.getParameter("fullName"));
        String email = ValidationUtil.normalizeEmail(req.getParameter("email"));
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        req.setAttribute("submittedName", fullName);
        req.setAttribute("submittedEmail", email);

        if (fullName == null || fullName.length() < 2 || fullName.length() > 100) {
            req.setAttribute("formError", "Please enter a valid full name (2 to 100 characters).");
            req.getRequestDispatcher("/setup.jsp").forward(req, resp);
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            req.setAttribute("formError", "Please enter a valid email address.");
            req.getRequestDispatcher("/setup.jsp").forward(req, resp);
            return;
        }
        String policyError = PasswordUtil.validatePasswordPolicy(password);
        if (policyError != null) {
            req.setAttribute("formError", policyError);
            req.getRequestDispatcher("/setup.jsp").forward(req, resp);
            return;
        }
        if (!password.equals(confirmPassword)) {
            req.setAttribute("formError", "Password and confirmation password do not match.");
            req.getRequestDispatcher("/setup.jsp").forward(req, resp);
            return;
        }

        try {
            if (userDAO.emailExists(email)) {
                req.setAttribute("formError", "Email already registered. Please sign in or use a different email.");
                req.getRequestDispatcher("/setup.jsp").forward(req, resp);
                return;
            }

            String hash = PasswordUtil.hashPassword(password);
            User created = userDAO.createInitialAdminIfNoneExists(fullName, email, hash);
            if (created == null) {
                FlashUtil.setInfo(req, "Initial administrator setup was already completed.");
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            FlashUtil.setSuccess(req, "Initial Administrator account created successfully. Please sign in.");
            resp.sendRedirect(req.getContextPath() + "/login");
        } catch (SQLException ex) {
            getServletContext().log("Error creating initial admin account", ex);
            req.setAttribute("formError", "Unable to create administrator account due to a database error.");
            req.getRequestDispatcher("/setup.jsp").forward(req, resp);
        }
    }
}
