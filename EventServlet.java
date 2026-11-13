package com.campusflow.servlet;

import com.campusflow.dao.ClubDAO;
import com.campusflow.dao.EventDAO;
import com.campusflow.dao.RegistrationDAO;
import com.campusflow.model.Club;
import com.campusflow.model.Event;
import com.campusflow.model.User;
import com.campusflow.service.EventApprovalService;
import com.campusflow.util.AuthUtil;
import com.campusflow.util.FlashUtil;
import com.campusflow.util.ValidationUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Event listing, details, proposal submission, CRUD, and approval workflow controller (/events).
 */
public class EventServlet extends HttpServlet {

    private final EventDAO eventDAO = new EventDAO();
    private final ClubDAO clubDAO = new ClubDAO();
    private final RegistrationDAO registrationDAO = new RegistrationDAO();
    private final EventApprovalService approvalService = new EventApprovalService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer userId = AuthUtil.getUserId(req);
        String role = AuthUtil.getUserRole(req);
        Integer eventId = ValidationUtil.parsePositiveInt(req.getParameter("id"));

        try {
            if (eventId != null) {
                Event event = eventDAO.findById(eventId, userId);
                if (event == null) {
                    FlashUtil.setError(req, "Requested event was not found.");
                    resp.sendRedirect(req.getContextPath() + "/events");
                    return;
                }

                boolean isOrganizer = eventDAO.isAuthorizedOrganizerForEvent(eventId, userId, role);
                boolean isCreator = event.getCreatedBy() != null && event.getCreatedBy().equals(userId);
                boolean isPubliclyVisible = Set.of(
                        Event.STATUS_APPROVED, Event.STATUS_COMPLETED, Event.STATUS_SCHEDULE_PENDING
                ).contains(event.getStatus());

                if (!isPubliclyVisible && !User.ROLE_ADMIN.equals(role) && !isOrganizer && !isCreator) {
                    denyForbidden(req, resp);
                    return;
                }

                boolean canEditEvent = User.ROLE_ADMIN.equals(role)
                        || (User.ROLE_CLUB_LEADER.equals(role) && isOrganizer
                            && Event.STATUS_PENDING_APPROVAL.equals(event.getStatus()));
                boolean canReviewProposal = Event.STATUS_PENDING_APPROVAL.equals(event.getStatus())
                        && (User.ROLE_ADMIN.equals(role)
                            || (User.ROLE_FACULTY.equals(role) && isOrganizer && !isCreator));

                req.setAttribute("event", event);
                req.setAttribute("isOrganizer", isOrganizer);
                req.setAttribute("canEditEvent", canEditEvent);
                req.setAttribute("canReviewProposal", canReviewProposal);
                req.setAttribute("approvalHistory", eventDAO.findApprovalHistory(eventId));
                req.setAttribute("editableClubs", User.ROLE_ADMIN.equals(role)
                        ? clubDAO.findAllClubs(userId)
                        : clubDAO.findByLeader(userId));

                if (isOrganizer) {
                    req.setAttribute("participants", registrationDAO.findParticipantsByEvent(eventId));
                } else {
                    req.setAttribute("participants", List.of());
                }

                req.getRequestDispatcher("/event-details.jsp").forward(req, resp);
                return;
            }

            String statusFilter = ValidationUtil.trimOrNull(req.getParameter("status"));
            Integer clubIdFilter = ValidationUtil.parsePositiveInt(req.getParameter("clubId"));

            req.setAttribute("selectedStatus", statusFilter == null ? "ALL" : statusFilter);
            req.setAttribute("selectedClubId", clubIdFilter);
            req.setAttribute("events", eventDAO.findEvents(statusFilter, clubIdFilter, userId, role));
            req.setAttribute("allClubs", clubDAO.findAllClubs(userId));

            if (User.ROLE_CLUB_LEADER.equals(role)) {
                req.setAttribute("creatableClubs", clubDAO.findByLeader(userId));
            } else if (User.ROLE_ADMIN.equals(role)) {
                req.setAttribute("creatableClubs", clubDAO.findAllClubs(userId));
            } else {
                req.setAttribute("creatableClubs", List.of());
            }

            if (User.ROLE_ADMIN.equals(role) || User.ROLE_FACULTY.equals(role)) {
                req.setAttribute("pendingApprovals", eventDAO.findPendingApprovalsForReviewer(userId, role));
            } else {
                req.setAttribute("pendingApprovals", List.of());
            }

            req.getRequestDispatcher("/events.jsp").forward(req, resp);
        } catch (SQLException ex) {
            getServletContext().log("Database error in EventServlet GET", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            req.setAttribute("errorTitle", "Events Error");
            req.setAttribute("errorMessage", "Unable to retrieve events from MySQL.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = ValidationUtil.trimOrNull(req.getParameter("action"));
        Integer userId = AuthUtil.getUserId(req);
        String role = AuthUtil.getUserRole(req);

        if (action == null) {
            resp.sendRedirect(req.getContextPath() + "/events");
            return;
        }

        try {
            switch (action) {
                case "create" -> handleCreate(req, resp, userId, role);
                case "update" -> handleUpdate(req, resp, userId, role);
                case "review" -> handleReview(req, resp, userId, role);
                case "changeStatus" -> handleStatusChange(req, resp, role);
                case "delete" -> handleDelete(req, resp, role);
                default -> resp.sendRedirect(req.getContextPath() + "/events");
            }
        } catch (SQLException ex) {
            getServletContext().log("Database error in EventServlet POST (" + action + ")", ex);
            FlashUtil.setError(req, "Database operation failed while processing event request.");
            resp.sendRedirect(req.getContextPath() + "/events");
        }
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp, Integer userId, String role)
            throws SQLException, ServletException, IOException {
        boolean isAdmin = User.ROLE_ADMIN.equals(role);
        boolean isLeader = User.ROLE_CLUB_LEADER.equals(role);
        if (!isAdmin && !isLeader) {
            denyForbidden(req, resp);
            return;
        }

        Event parsed = new Event();
        String validationError = populateAndValidateEventFields(req, parsed);
        if (validationError != null) {
            FlashUtil.setError(req, validationError);
            resp.sendRedirect(req.getContextPath() + "/events");
            return;
        }

        List<Integer> selectedClubs = parseClubIds(req.getParameterValues("clubIds"));
        if (selectedClubs.isEmpty()) {
            FlashUtil.setError(req, "Please select at least one associated club.");
            resp.sendRedirect(req.getContextPath() + "/events");
            return;
        }

        if (isLeader) {
            Set<Integer> allowedLeaderClubIds = new HashSet<>();
            for (Club c : clubDAO.findByLeader(userId)) {
                allowedLeaderClubIds.add(c.getClubId());
            }
            if (allowedLeaderClubIds.isEmpty() || !allowedLeaderClubIds.containsAll(selectedClubs)) {
                denyForbidden(req, resp);
                return;
            }
            // Club Leader submissions always enter Pending Approval
            parsed.setStatus(Event.STATUS_PENDING_APPROVAL);
        } else {
            parsed.setStatus(Event.STATUS_APPROVED);
        }

        parsed.setFlagship(false);
        parsed.setCreatedBy(userId);

        int newEventId = eventDAO.createEvent(parsed, selectedClubs);
        if (isLeader) {
            FlashUtil.setSuccess(req, "Event proposal submitted and marked as Pending Approval.");
        } else {
            FlashUtil.setSuccess(req, "Event created successfully.");
        }
        resp.sendRedirect(req.getContextPath() + "/events?id=" + newEventId);
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp, Integer userId, String role)
            throws SQLException, ServletException, IOException {
        Integer eventId = ValidationUtil.parsePositiveInt(req.getParameter("eventId"));
        if (eventId == null) {
            resp.sendRedirect(req.getContextPath() + "/events");
            return;
        }

        Event existing = eventDAO.findById(eventId, userId);
        if (existing == null) {
            FlashUtil.setError(req, "Event not found.");
            resp.sendRedirect(req.getContextPath() + "/events");
            return;
        }
        if (existing.isFlagship()) {
            resp.sendRedirect(req.getContextPath() + "/lakshya");
            return;
        }

        boolean isAdmin = User.ROLE_ADMIN.equals(role);
        boolean isAuthorizedLeader = User.ROLE_CLUB_LEADER.equals(role)
                && Event.STATUS_PENDING_APPROVAL.equals(existing.getStatus())
                && eventDAO.isAuthorizedOrganizerForEvent(eventId, userId, role);

        if (!isAdmin && !isAuthorizedLeader) {
            denyForbidden(req, resp);
            return;
        }

        String validationError = populateAndValidateEventFields(req, existing);
        if (validationError != null) {
            FlashUtil.setError(req, validationError);
            resp.sendRedirect(req.getContextPath() + "/events?id=" + eventId);
            return;
        }

        List<Integer> selectedClubs = parseClubIds(req.getParameterValues("clubIds"));
        if (selectedClubs.isEmpty()) {
            FlashUtil.setError(req, "Please select at least one associated club.");
            resp.sendRedirect(req.getContextPath() + "/events?id=" + eventId);
            return;
        }

        if (isAuthorizedLeader) {
            Set<Integer> allowedLeaderClubIds = new HashSet<>();
            for (Club c : clubDAO.findByLeader(userId)) {
                allowedLeaderClubIds.add(c.getClubId());
            }
            if (!allowedLeaderClubIds.containsAll(selectedClubs)) {
                denyForbidden(req, resp);
                return;
            }
            existing.setStatus(Event.STATUS_PENDING_APPROVAL);
        }

        eventDAO.updateEvent(existing, selectedClubs);
        FlashUtil.setSuccess(req, "Event updated successfully.");
        resp.sendRedirect(req.getContextPath() + "/events?id=" + eventId);
    }

    private void handleReview(HttpServletRequest req, HttpServletResponse resp, Integer userId, String role)
            throws SQLException, IOException {
        Integer eventId = ValidationUtil.parsePositiveInt(req.getParameter("eventId"));
        String decision = ValidationUtil.trimOrNull(req.getParameter("decision"));
        String remarks = ValidationUtil.trimOrNull(req.getParameter("remarks"));
        if (remarks != null && remarks.length() > 500) {
            remarks = remarks.substring(0, 500);
        }

        if (eventId == null) {
            resp.sendRedirect(req.getContextPath() + "/events");
            return;
        }

        EventApprovalService.Outcome outcome =
                approvalService.reviewProposal(eventId, userId, role, decision, remarks);
        if (outcome.success()) {
            FlashUtil.setSuccess(req, outcome.message());
        } else {
            FlashUtil.setError(req, outcome.message());
        }
        resp.sendRedirect(req.getContextPath() + "/events?id=" + eventId);
    }

    private void handleStatusChange(HttpServletRequest req, HttpServletResponse resp, String role)
            throws SQLException, ServletException, IOException {
        if (!User.ROLE_ADMIN.equals(role)) {
            denyForbidden(req, resp);
            return;
        }

        Integer eventId = ValidationUtil.parsePositiveInt(req.getParameter("eventId"));
        String newStatus = ValidationUtil.trimOrNull(req.getParameter("newStatus"));
        if (eventId == null || newStatus == null
                || !Set.of(Event.STATUS_APPROVED, Event.STATUS_COMPLETED, Event.STATUS_CANCELLED).contains(newStatus)) {
            FlashUtil.setError(req, "Invalid status transition requested.");
            resp.sendRedirect(req.getContextPath() + "/events");
            return;
        }

        Event existing = eventDAO.findById(eventId, null);
        if (existing == null) {
            FlashUtil.setError(req, "Event not found.");
            resp.sendRedirect(req.getContextPath() + "/events");
            return;
        }
        if (Event.STATUS_APPROVED.equals(newStatus) && (existing.getEventDate() == null || existing.getVenue() == null)) {
            FlashUtil.setError(req, "Event must have an official date and venue before marking as Approved.");
            resp.sendRedirect(req.getContextPath() + "/events?id=" + eventId);
            return;
        }

        eventDAO.updateEventStatus(eventId, newStatus);
        FlashUtil.setSuccess(req, "Event status updated to " + newStatus + ".");
        resp.sendRedirect(req.getContextPath() + "/events?id=" + eventId);
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp, String role)
            throws SQLException, ServletException, IOException {
        if (!User.ROLE_ADMIN.equals(role)) {
            denyForbidden(req, resp);
            return;
        }

        Integer eventId = ValidationUtil.parsePositiveInt(req.getParameter("eventId"));
        if (eventId == null) {
            resp.sendRedirect(req.getContextPath() + "/events");
            return;
        }

        Event existing = eventDAO.findById(eventId, null);
        if (existing == null) {
            FlashUtil.setError(req, "Event not found.");
            resp.sendRedirect(req.getContextPath() + "/events");
            return;
        }
        if (existing.isFlagship()) {
            FlashUtil.setError(req, "Primary flagship event Lakshya cannot be deleted.");
            resp.sendRedirect(req.getContextPath() + "/lakshya");
            return;
        }
        if (eventDAO.hasRegistrationsOrAttendance(eventId)) {
            FlashUtil.setError(req, "Cannot delete an event that has student registrations or attendance records. Mark it as Cancelled instead.");
            resp.sendRedirect(req.getContextPath() + "/events?id=" + eventId);
            return;
        }

        eventDAO.deleteEvent(eventId);
        FlashUtil.setSuccess(req, "Event deleted successfully.");
        resp.sendRedirect(req.getContextPath() + "/events");
    }

    private String populateAndValidateEventFields(HttpServletRequest req, Event target) {
        String eventName = ValidationUtil.trimOrNull(req.getParameter("eventName"));
        String description = ValidationUtil.trimOrNull(req.getParameter("description"));
        LocalDate eventDate = ValidationUtil.parseDate(req.getParameter("eventDate"));
        LocalTime startTime = ValidationUtil.parseTime(req.getParameter("startTime"));
        LocalTime endTime = ValidationUtil.parseTime(req.getParameter("endTime"));
        String venue = ValidationUtil.trimOrNull(req.getParameter("venue"));
        String organizer = ValidationUtil.trimOrNull(req.getParameter("organizer"));
        LocalDateTime deadline = ValidationUtil.parseDateTimeLocal(req.getParameter("registrationDeadline"));

        if (eventName == null || eventName.length() > 150) {
            return "Event name is required (maximum 150 characters).";
        }
        if (description == null) {
            return "Event description is required.";
        }
        if (eventDate == null) {
            return "Valid event date is required.";
        }
        if (venue == null || venue.length() > 180) {
            return "Event venue is required (maximum 180 characters).";
        }
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            return "End time must be later than start time.";
        }
        if (deadline != null && deadline.toLocalDate().isAfter(eventDate)) {
            return "Registration deadline cannot be after the event date.";
        }

        String rawCap = ValidationUtil.trimOrNull(req.getParameter("capacity"));
        Integer capacity = null;
        if (rawCap != null) {
            capacity = ValidationUtil.parsePositiveInt(rawCap);
            if (capacity == null) {
                return "Capacity must be a positive whole number (or left blank for unlimited).";
            }
        }

        target.setEventName(eventName);
        target.setDescription(description);
        target.setEventDate(eventDate);
        target.setStartTime(startTime);
        target.setEndTime(endTime);
        target.setVenue(venue);
        target.setOrganizer(organizer);
        target.setRegistrationDeadline(deadline);
        target.setCapacity(capacity);
        return null;
    }

    private List<Integer> parseClubIds(String[] rawIds) {
        List<Integer> list = new ArrayList<>();
        if (rawIds == null) {
            return list;
        }
        for (String r : rawIds) {
            Integer id = ValidationUtil.parsePositiveInt(r);
            if (id != null && !list.contains(id)) {
                list.add(id);
            }
        }
        return list;
    }

    private void denyForbidden(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        req.setAttribute("errorTitle", "403 — Access Denied");
        req.setAttribute("errorMessage", "You do not have permission to perform this action.");
        req.getRequestDispatcher("/error.jsp").forward(req, resp);
    }
}
