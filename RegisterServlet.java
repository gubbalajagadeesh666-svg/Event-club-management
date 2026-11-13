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
 * Public student registration controller (/register).
 * Always assigns the STUDENT role on the server regardless of any browser-supplied role parameter.
 */
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (AuthUtil.isAuthenticated(req)) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (AuthUtil.isAuthenticated(req)) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        String fullName = ValidationUtil.trimOrNull(req.getParameter("fullName"));
        String email = ValidationUtil.normalizeEmail(req.getParameter("email"));
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        req.setAttribute("submittedName", fullName);
        req.setAttribute("submittedEmail", email);

        if (fullName == null || fullName.length() < 2 || fullName.length() > 100) {
            req.setAttribute("formError", "Full name is required and must be between 2 and 100 characters.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            req.setAttribute("formError", "Please provide a valid email address (up to 150 characters).");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }
        String policyError = PasswordUtil.validatePasswordPolicy(password);
        if (policyError != null) {
            req.setAttribute("formError", policyError);
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }
        if (!password.equals(confirmPassword)) {
            req.setAttribute("formError", "Password and confirm password do not match.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        try {
            if (userDAO.emailExists(email)) {
                req.setAttribute("formError", "Email already registered.");
                req.getRequestDispatcher("/register.jsp").forward(req, resp);
                return;
            }

            String passwordHash = PasswordUtil.hashPassword(password);
            // Server strictly assigns STUDENT role — ignores any client-submitted role value
            userDAO.createUser(fullName, email, passwordHash, User.ROLE_STUDENT, User.STATUS_ACTIVE);

            FlashUtil.setSuccess(req, "Registration successful. Please log in with your credentials.");
            resp.sendRedirect(req.getContextPath() + "/login");
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062) {
                req.setAttribute("formError", "Email already registered.");
            } else {
                getServletContext().log("Database error during student registration", ex);
                req.setAttribute("formError", "Unable to complete registration right now. Please try again.");
            }
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
        }
    }
}
