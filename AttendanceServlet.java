package com.campusflow.servlet;

import com.campusflow.dao.AttendanceDAO;
import com.campusflow.dao.EventDAO;
import com.campusflow.dao.RegistrationDAO;
import com.campusflow.model.Event;
import com.campusflow.model.EventRegistration;
import com.campusflow.util.AuthUtil;
import com.campusflow.util.FlashUtil;
import com.campusflow.util.ValidationUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Organizer attendance management controller (/attendance).
 * Enforces server-side organizer permission before viewing roster or saving attendance.
 */
public class AttendanceServlet extends HttpServlet {

    private final EventDAO eventDAO = new EventDAO();
    private final RegistrationDAO registrationDAO = new RegistrationDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer userId = AuthUtil.getUserId(req);
        String role = AuthUtil.getUserRole(req);
        Integer eventId = ValidationUtil.parsePositiveInt(req.getParameter("eventId"));

        if (eventId == null) {
            resp.sendRedirect(req.getContextPath() + "/events");
            return;
        }

        try {
            Event event = eventDAO.findById(eventId, userId);
            if (event == null) {
                FlashUtil.setError(req, "Event not found.");
                resp.sendRedirect(req.getContextPath() + "/events");
                return;
            }
            if (!eventDAO.isAuthorizedOrganizerForEvent(eventId, userId, role)) {
                denyForbidden(req, resp);
                return;
            }

            List<EventRegistration> participants = registrationDAO.findParticipantsByEvent(eventId);
            req.setAttribute("event", event);
            req.setAttribute("participants", participants);
            req.getRequestDispatcher("/attendance.jsp").forward(req, resp);
        } catch (SQLException ex) {
            getServletContext().log("Database error loading attendance roster", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            req.setAttribute("errorTitle", "Attendance Error");
            req.setAttribute("errorMessage", "Unable to load participant attendance roster.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer userId = AuthUtil.getUserId(req);
        String role = AuthUtil.getUserRole(req);
        Integer eventId = ValidationUtil.parsePositiveInt(req.getParameter("eventId"));

        if (eventId == null) {
            resp.sendRedirect(req.getContextPath() + "/events");
            return;
        }

        try {
            if (!eventDAO.isAuthorizedOrganizerForEvent(eventId, userId, role)) {
                denyForbidden(req, resp);
                return;
            }

            List<EventRegistration> roster = registrationDAO.findParticipantsByEvent(eventId);
            Map<Integer, String> updates = new LinkedHashMap<>();
            for (EventRegistration reg : roster) {
                String val = ValidationUtil.trimOrNull(req.getParameter("status_" + reg.getUserId()));
                if ("PRESENT".equals(val) || "ABSENT".equals(val)) {
                    updates.put(reg.getUserId(), val);
                }
            }

            int saved = attendanceDAO.saveAttendanceForEvent(eventId, userId, updates);
            FlashUtil.setSuccess(req, "Attendance saved for " + saved + " participant(s).");
            resp.sendRedirect(req.getContextPath() + "/attendance?eventId=" + eventId);
        } catch (SQLException ex) {
            getServletContext().log("Database error saving attendance", ex);
            FlashUtil.setError(req, "Unable to save attendance records.");
            resp.sendRedirect(req.getContextPath() + "/attendance?eventId=" + eventId);
        }
    }

    private void denyForbidden(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        req.setAttribute("errorTitle", "403 — Access Denied");
        req.setAttribute("errorMessage", "You do not have permission to manage attendance for this event.");
        req.getRequestDispatcher("/error.jsp").forward(req, resp);
    }
}
