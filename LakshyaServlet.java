package com.campusflow.servlet;

import com.campusflow.dao.ClubDAO;
import com.campusflow.dao.EventDAO;
import com.campusflow.dao.RegistrationDAO;
import com.campusflow.model.Event;
import com.campusflow.model.User;
import com.campusflow.util.AuthUtil;
import com.campusflow.util.FlashUtil;
import com.campusflow.util.ValidationUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Dedicated controller (/lakshya) for the primary college flagship event Lakshya.
 * Reads all details dynamically from the events table in MySQL without fabricating
 * dates, venues, or organizers. Allows authorized Administrators to publish/update
 * official Lakshya details.
 */
public class LakshyaServlet extends HttpServlet {

    private static final Set<String> ALLOWED_LAKSHYA_STATUSES = Set.of(
            Event.STATUS_SCHEDULE_PENDING,
            Event.STATUS_APPROVED,
            Event.STATUS_COMPLETED,
            Event.STATUS_CANCELLED
    );

    private final EventDAO eventDAO = new EventDAO();
    private final ClubDAO clubDAO = new ClubDAO();
    private final RegistrationDAO registrationDAO = new RegistrationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer userId = AuthUtil.getUserId(req);
        String role = AuthUtil.getUserRole(req);

        try {
            Event lakshya = eventDAO.findLakshya(userId);
            req.setAttribute("lakshya", lakshya);
            req.setAttribute("allClubs", clubDAO.findAllClubs(userId));

            if (lakshya != null) {
                boolean canViewParticipants = eventDAO.isAuthorizedOrganizerForEvent(
                        lakshya.getEventId(), userId, role);
                req.setAttribute("canViewParticipants", canViewParticipants);
                if (canViewParticipants) {
                    req.setAttribute("participants",
                            registrationDAO.findParticipantsByEvent(lakshya.getEventId()));
                } else {
                    req.setAttribute("participants", List.of());
                }
            }

            req.getRequestDispatcher("/lakshya.jsp").forward(req, resp);
        } catch (SQLException ex) {
            getServletContext().log("Database error loading Lakshya page", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            req.setAttribute("errorTitle", "Lakshya Page Error");
            req.setAttribute("errorMessage", "Unable to retrieve Lakshya details from MySQL.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String role = AuthUtil.getUserRole(req);
        if (!User.ROLE_ADMIN.equals(role)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            req.setAttribute("errorTitle", "403 — Access Denied");
            req.setAttribute("errorMessage", "Only an Administrator can update official Lakshya details.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
            return;
        }

        try {
            Event lakshya = eventDAO.findLakshya(AuthUtil.getUserId(req));
            if (lakshya == null) {
                FlashUtil.setError(req, "Lakshya record not found in the database.");
                resp.sendRedirect(req.getContextPath() + "/lakshya");
                return;
            }

            String description = ValidationUtil.trimOrNull(req.getParameter("description"));
            LocalDate eventDate = ValidationUtil.parseDate(req.getParameter("eventDate"));
            LocalTime startTime = ValidationUtil.parseTime(req.getParameter("startTime"));
            LocalTime endTime = ValidationUtil.parseTime(req.getParameter("endTime"));
            String venue = ValidationUtil.trimOrNull(req.getParameter("venue"));
            String organizer = ValidationUtil.trimOrNull(req.getParameter("organizer"));
            LocalDateTime deadline = ValidationUtil.parseDateTimeLocal(req.getParameter("registrationDeadline"));
            String rawCap = ValidationUtil.trimOrNull(req.getParameter("capacity"));
            Integer capacity = null;
            if (rawCap != null) {
                capacity = ValidationUtil.parsePositiveInt(rawCap);
                if (capacity == null) {
                    FlashUtil.setError(req, "Capacity must be a positive whole number (or left blank for unlimited).");
                    resp.sendRedirect(req.getContextPath() + "/lakshya");
                    return;
                }
            }

            String status = ValidationUtil.trimOrNull(req.getParameter("status"));
            if (status == null || !ALLOWED_LAKSHYA_STATUSES.contains(status)) {
                status = lakshya.getStatus();
            }

            if (description == null) {
                FlashUtil.setError(req, "Lakshya description cannot be empty.");
                resp.sendRedirect(req.getContextPath() + "/lakshya");
                return;
            }

            if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
                FlashUtil.setError(req, "End time must be after start time.");
                resp.sendRedirect(req.getContextPath() + "/lakshya");
                return;
            }

            if (Event.STATUS_APPROVED.equals(status) && (eventDate == null || venue == null)) {
                FlashUtil.setError(req, "To open Lakshya registration (Approved), please specify both Official Date and Venue.");
                resp.sendRedirect(req.getContextPath() + "/lakshya");
                return;
            }

            List<Integer> clubIds = parseClubIds(req.getParameterValues("clubIds"));
            if (clubIds.isEmpty()) {
                FlashUtil.setError(req, "Please associate at least one participating club with Lakshya.");
                resp.sendRedirect(req.getContextPath() + "/lakshya");
                return;
            }

            lakshya.setDescription(description);
            lakshya.setEventDate(eventDate);
            lakshya.setStartTime(startTime);
            lakshya.setEndTime(endTime);
            lakshya.setVenue(venue);
            lakshya.setOrganizer(organizer);
            lakshya.setRegistrationDeadline(deadline);
            lakshya.setCapacity(capacity);
            lakshya.setStatus(status);

            eventDAO.updateEvent(lakshya, clubIds);
            FlashUtil.setSuccess(req, "Lakshya official event details updated successfully.");
            resp.sendRedirect(req.getContextPath() + "/lakshya");
        } catch (SQLException ex) {
            getServletContext().log("Database error updating Lakshya", ex);
            FlashUtil.setError(req, "Failed to save Lakshya updates to MySQL.");
            resp.sendRedirect(req.getContextPath() + "/lakshya");
        }
    }

    private List<Integer> parseClubIds(String[] rawIds) {
        List<Integer> result = new ArrayList<>();
        if (rawIds == null) {
            return result;
        }
        for (String r : rawIds) {
            Integer parsed = ValidationUtil.parsePositiveInt(r);
            if (parsed != null && !result.contains(parsed)) {
                result.add(parsed);
            }
        }
        return result;
    }
}
