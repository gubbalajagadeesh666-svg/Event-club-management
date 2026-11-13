package com.campusflow.servlet;

import com.campusflow.dao.RegistrationDAO;
import com.campusflow.model.User;
import com.campusflow.service.RegistrationService;
import com.campusflow.util.AuthUtil;
import com.campusflow.util.FlashUtil;
import com.campusflow.util.ValidationUtil;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Student event registration and "My Registrations & Attendance" controller (/registrations).
 */
public class RegistrationServlet extends HttpServlet {

    private final RegistrationDAO registrationDAO = new RegistrationDAO();
    private final RegistrationService registrationService = new RegistrationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer userId = AuthUtil.getUserId(req);
        try {
            req.setAttribute("registrations", registrationDAO.findByStudent(userId));
            req.getRequestDispatcher("/my-registrations.jsp").forward(req, resp);
        } catch (SQLException ex) {
            getServletContext().log("Database error loading student registrations", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            req.setAttribute("errorTitle", "Registrations Error");
            req.setAttribute("errorMessage", "Unable to load your registrations from MySQL.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer userId = AuthUtil.getUserId(req);
        String role = AuthUtil.getUserRole(req);
        String action = ValidationUtil.trimOrNull(req.getParameter("action"));
        Integer eventId = ValidationUtil.parsePositiveInt(req.getParameter("eventId"));
        String returnTo = ValidationUtil.trimOrNull(req.getParameter("returnTo"));

        if (!User.ROLE_STUDENT.equals(role) && !User.ROLE_CLUB_LEADER.equals(role)) {
            FlashUtil.setError(req, "Participant registration is available to student accounts.");
            resp.sendRedirect(resolveSafeRedirect(req, returnTo, eventId));
            return;
        }

        if (eventId == null) {
            FlashUtil.setError(req, "Invalid event selected.");
            resp.sendRedirect(req.getContextPath() + "/events");
            return;
        }

        try {
            RegistrationService.Outcome outcome;
            if ("cancel".equals(action)) {
                outcome = registrationService.cancelRegistration(eventId, userId);
            } else {
                outcome = registrationService.registerStudent(eventId, userId);
            }

            if (outcome.success()) {
                FlashUtil.setSuccess(req, outcome.message());
            } else {
                FlashUtil.setError(req, outcome.message());
            }
            resp.sendRedirect(resolveSafeRedirect(req, returnTo, eventId));
        } catch (SQLException ex) {
            getServletContext().log("Database error processing registration", ex);
            FlashUtil.setError(req, "Unable to process event registration right now.");
            resp.sendRedirect(resolveSafeRedirect(req, returnTo, eventId));
        }
    }

    private String resolveSafeRedirect(HttpServletRequest req, String returnTo, Integer eventId) {
        String ctx = req.getContextPath();
        if ("lakshya".equals(returnTo)) {
            return ctx + "/lakshya";
        }
        if ("my".equals(returnTo)) {
            return ctx + "/registrations";
        }
        if (eventId != null) {
            return ctx + "/events?id=" + eventId;
        }
        return ctx + "/events";
    }
}
