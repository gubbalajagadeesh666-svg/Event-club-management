package com.campusflow.servlet;

import com.campusflow.dao.UserDAO;
import com.campusflow.model.User;
import com.campusflow.util.AuthUtil;
import com.campusflow.util.FlashUtil;
import com.campusflow.util.PasswordUtil;
import com.campusflow.util.ValidationUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Administrator user & role management controller (/admin/users).
 * Strictly enforces ADMIN role on the server.
 */
public class AdminUserServlet extends HttpServlet {

    private static final Set<String> VALID_ROLES = Set.of(
            User.ROLE_ADMIN, User.ROLE_FACULTY, User.ROLE_CLUB_LEADER, User.ROLE_STUDENT
    );
    private static final Set<String> VALID_STATUSES = Set.of(
            User.STATUS_ACTIVE, User.STATUS_INACTIVE
    );

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!AuthUtil.isAdmin(req)) {
            denyForbidden(req, resp);
            return;
        }

        try {
            req.setAttribute("users", userDAO.findAllUsers());
            req.getRequestDispatcher("/admin-users.jsp").forward(req, resp);
        } catch (SQLException ex) {
            getServletContext().log("Database error loading admin user list", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            req.setAttribute("errorTitle", "User Administration Error");
            req.setAttribute("errorMessage", "Unable to retrieve user accounts from MySQL.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!AuthUtil.isAdmin(req)) {
            denyForbidden(req, resp);
            return;
        }

        String action = ValidationUtil.trimOrNull(req.getParameter("action"));
        Integer currentAdminId = AuthUtil.getUserId(req);

        try {
            if ("createUser".equals(action)) {
                handleCreateUser(req, resp);
            } else if ("updateUser".equals(action)) {
                handleUpdateUser(req, resp, currentAdminId);
            } else {
                resp.sendRedirect(req.getContextPath() + "/admin/users");
            }
        } catch (SQLException ex) {
            getServletContext().log("Database error in AdminUserServlet POST", ex);
            if (ex.getErrorCode() == 1062) {
                FlashUtil.setError(req, "Email already registered.");
            } else {
                FlashUtil.setError(req, "Unable to complete user account operation.");
            }
            resp.sendRedirect(req.getContextPath() + "/admin/users");
        }
    }

    private void handleCreateUser(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        String fullName = ValidationUtil.trimOrNull(req.getParameter("fullName"));
        String email = ValidationUtil.normalizeEmail(req.getParameter("email"));
        String password = req.getParameter("password");
        String role = ValidationUtil.trimOrNull(req.getParameter("role"));

        if (fullName == null || fullName.length() < 2 || fullName.length() > 100) {
            FlashUtil.setError(req, "Full name must be between 2 and 100 characters.");
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            FlashUtil.setError(req, "Please provide a valid email address.");
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }
        if (role == null || !VALID_ROLES.contains(role)) {
            FlashUtil.setError(req, "Invalid user role selected.");
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }
        String policyErr = PasswordUtil.validatePasswordPolicy(password);
        if (policyErr != null) {
            FlashUtil.setError(req, policyErr);
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }
        if (userDAO.emailExists(email)) {
            FlashUtil.setError(req, "Email already registered.");
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }

        String hash = PasswordUtil.hashPassword(password);
        userDAO.createUser(fullName, email, hash, role, User.STATUS_ACTIVE);
        FlashUtil.setSuccess(req, "Account created for " + fullName + " (" + role + ").");
        resp.sendRedirect(req.getContextPath() + "/admin/users");
    }

    private void handleUpdateUser(HttpServletRequest req, HttpServletResponse resp, Integer currentAdminId)
            throws SQLException, IOException {
        Integer targetUserId = ValidationUtil.parsePositiveInt(req.getParameter("userId"));
        String newRole = ValidationUtil.trimOrNull(req.getParameter("role"));
        String newStatus = ValidationUtil.trimOrNull(req.getParameter("status"));

        if (targetUserId == null || newRole == null || !VALID_ROLES.contains(newRole)
                || newStatus == null || !VALID_STATUSES.contains(newStatus)) {
            FlashUtil.setError(req, "Invalid role or status values submitted.");
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }

        User target = userDAO.findById(targetUserId);
        if (target == null) {
            FlashUtil.setError(req, "Target user account not found.");
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }

        // Prevent self-lockout or removing the last active Administrator
        if (targetUserId.equals(currentAdminId)
                && (!User.ROLE_ADMIN.equals(newRole) || !User.STATUS_ACTIVE.equals(newStatus))) {
            FlashUtil.setError(req, "You cannot remove Administrator role or deactivate your own signed-in account.");
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }

        if (User.ROLE_ADMIN.equals(target.getRole()) && target.isActive()
                && (!User.ROLE_ADMIN.equals(newRole) || !User.STATUS_ACTIVE.equals(newStatus))) {
            if (userDAO.countActiveAdmins() <= 1) {
                FlashUtil.setError(req, "At least one active Administrator account must remain in the system.");
                resp.sendRedirect(req.getContextPath() + "/admin/users");
                return;
            }
        }

        userDAO.updateRoleAndStatus(targetUserId, newRole, newStatus);
        FlashUtil.setSuccess(req, "Updated role/status for " + target.getFullName() + ".");
        resp.sendRedirect(req.getContextPath() + "/admin/users");
    }

    private void denyForbidden(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        req.setAttribute("errorTitle", "403 — Access Denied");
        req.setAttribute("errorMessage", "Only an authorized Administrator can manage user accounts.");
        req.getRequestDispatcher("/error.jsp").forward(req, resp);
    }
}
