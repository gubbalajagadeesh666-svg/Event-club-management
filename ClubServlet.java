package com.campusflow.servlet;

import com.campusflow.dao.ClubDAO;
import com.campusflow.dao.EventDAO;
import com.campusflow.dao.UserDAO;
import com.campusflow.model.Club;
import com.campusflow.model.User;
import com.campusflow.util.AuthUtil;
import com.campusflow.util.FlashUtil;
import com.campusflow.util.ValidationUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Club listing, details, membership, and role-authorized CRUD controller (/clubs).
 */
public class ClubServlet extends HttpServlet {

    private static final Set<String> ALLOWED_THEMES =
            Set.of("emerald", "sky", "amber", "rose", "indigo");
    private static final Set<String> ALLOWED_ICONS = Set.of(
            "bi-tree-fill", "bi-journal-richtext", "bi-music-note-beamed",
            "bi-heart-pulse-fill", "bi-stars", "bi-cpu-fill", "bi-camera-reels-fill",
            "bi-trophy-fill", "bi-globe-central-south-asia", "bi-lightbulb-fill"
    );

    private final ClubDAO clubDAO = new ClubDAO();
    private final EventDAO eventDAO = new EventDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer userId = AuthUtil.getUserId(req);
        String role = AuthUtil.getUserRole(req);
        Integer clubId = ValidationUtil.parsePositiveInt(req.getParameter("id"));

        try {
            if (clubId != null) {
                Club club = clubDAO.findById(clubId, userId);
                if (club == null) {
                    FlashUtil.setError(req, "Requested club was not found.");
                    resp.sendRedirect(req.getContextPath() + "/clubs");
                    return;
                }

                boolean canManageClub = User.ROLE_ADMIN.equals(role)
                        || (User.ROLE_CLUB_LEADER.equals(role)
                            && club.getLeaderId() != null && club.getLeaderId().equals(userId));
                boolean canViewMembers = canManageClub
                        || (User.ROLE_FACULTY.equals(role)
                            && club.getCoordinatorId() != null && club.getCoordinatorId().equals(userId));

                req.setAttribute("club", club);
                req.setAttribute("canManageClub", canManageClub);
                req.setAttribute("canViewMembers", canViewMembers);
                req.setAttribute("clubEvents", eventDAO.findEventsByClub(clubId, userId, canViewMembers));

                if (canViewMembers) {
                    req.setAttribute("clubMembers", clubDAO.findActiveMembers(clubId));
                } else {
                    req.setAttribute("clubMembers", List.of());
                }

                if (User.ROLE_ADMIN.equals(role)) {
                    populateLeaderAndCoordinatorOptions(req);
                }

                req.getRequestDispatcher("/club-details.jsp").forward(req, resp);
                return;
            }

            req.setAttribute("clubs", clubDAO.findAllClubs(userId));
            if (User.ROLE_ADMIN.equals(role)) {
                populateLeaderAndCoordinatorOptions(req);
            }
            req.getRequestDispatcher("/clubs.jsp").forward(req, resp);
        } catch (SQLException ex) {
            getServletContext().log("Database error in ClubServlet GET", ex);
            sendServerError(req, resp, "Unable to load club records from MySQL.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = ValidationUtil.trimOrNull(req.getParameter("action"));
        Integer userId = AuthUtil.getUserId(req);
        String role = AuthUtil.getUserRole(req);

        if (action == null) {
            resp.sendRedirect(req.getContextPath() + "/clubs");
            return;
        }

        try {
            switch (action) {
                case "create" -> handleCreate(req, resp, role);
                case "update" -> handleUpdate(req, resp, userId, role);
                case "delete" -> handleDelete(req, resp, role);
                case "join" -> handleMembership(req, resp, userId, true);
                case "leave" -> handleMembership(req, resp, userId, false);
                default -> resp.sendRedirect(req.getContextPath() + "/clubs");
            }
        } catch (SQLException ex) {
            getServletContext().log("Database error in ClubServlet POST (" + action + ")", ex);
            if (ex.getErrorCode() == 1062) {
                FlashUtil.setError(req, "A club with that name already exists.");
            } else {
                FlashUtil.setError(req, "Database operation failed. Please verify your inputs and try again.");
            }
            resp.sendRedirect(req.getContextPath() + "/clubs");
        }
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp, String role)
            throws SQLException, ServletException, IOException {
        if (!User.ROLE_ADMIN.equals(role)) {
            denyForbidden(req, resp);
            return;
        }

        String clubName = ValidationUtil.trimOrNull(req.getParameter("clubName"));
        String focusArea = ValidationUtil.trimOrNull(req.getParameter("focusArea"));
        String description = ValidationUtil.trimOrNull(req.getParameter("description"));
        String activities = ValidationUtil.trimOrNull(req.getParameter("activities"));
        String iconClass = sanitizeChoice(req.getParameter("iconClass"), ALLOWED_ICONS, "bi-stars");
        String accentTheme = sanitizeChoice(req.getParameter("accentTheme"), ALLOWED_THEMES, "indigo");
        Integer coordinatorId = ValidationUtil.parsePositiveInt(req.getParameter("coordinatorId"));
        Integer leaderId = ValidationUtil.parsePositiveInt(req.getParameter("leaderId"));

        if (clubName == null || clubName.length() > 100
                || focusArea == null || focusArea.length() > 220
                || description == null || activities == null) {
            FlashUtil.setError(req, "All required club fields must be filled in within length limits.");
            resp.sendRedirect(req.getContextPath() + "/clubs");
            return;
        }

        Club club = new Club();
        club.setClubSlug(ValidationUtil.slugify(clubName));
        club.setClubName(clubName);
        club.setFocusArea(focusArea);
        club.setDescription(description);
        club.setActivities(activities);
        club.setIconClass(iconClass);
        club.setAccentTheme(accentTheme);
        club.setCoordinatorId(coordinatorId);
        club.setLeaderId(leaderId);

        int newId = clubDAO.createClub(club);
        FlashUtil.setSuccess(req, "Club '" + clubName + "' created successfully.");
        resp.sendRedirect(req.getContextPath() + "/clubs?id=" + newId);
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp, Integer userId, String role)
            throws SQLException, ServletException, IOException {
        Integer clubId = ValidationUtil.parsePositiveInt(req.getParameter("clubId"));
        if (clubId == null) {
            FlashUtil.setError(req, "Invalid club identifier.");
            resp.sendRedirect(req.getContextPath() + "/clubs");
            return;
        }

        Club existing = clubDAO.findById(clubId, userId);
        if (existing == null) {
            FlashUtil.setError(req, "Club not found.");
            resp.sendRedirect(req.getContextPath() + "/clubs");
            return;
        }

        String focusArea = ValidationUtil.trimOrNull(req.getParameter("focusArea"));
        String description = ValidationUtil.trimOrNull(req.getParameter("description"));
        String activities = ValidationUtil.trimOrNull(req.getParameter("activities"));

        if (focusArea == null || focusArea.length() > 220 || description == null || activities == null) {
            FlashUtil.setError(req, "Focus area, description, and activities are required.");
            resp.sendRedirect(req.getContextPath() + "/clubs?id=" + clubId);
            return;
        }

        if (User.ROLE_ADMIN.equals(role)) {
            String clubName = ValidationUtil.trimOrNull(req.getParameter("clubName"));
            if (clubName == null || clubName.length() > 100) {
                FlashUtil.setError(req, "Valid club name is required.");
                resp.sendRedirect(req.getContextPath() + "/clubs?id=" + clubId);
                return;
            }
            existing.setClubName(clubName);
            existing.setFocusArea(focusArea);
            existing.setDescription(description);
            existing.setActivities(activities);
            existing.setIconClass(sanitizeChoice(req.getParameter("iconClass"), ALLOWED_ICONS, existing.getIconClass()));
            existing.setAccentTheme(sanitizeChoice(req.getParameter("accentTheme"), ALLOWED_THEMES, existing.getAccentTheme()));
            existing.setCoordinatorId(ValidationUtil.parsePositiveInt(req.getParameter("coordinatorId")));
            existing.setLeaderId(ValidationUtil.parsePositiveInt(req.getParameter("leaderId")));

            clubDAO.updateClubByAdmin(existing);
            FlashUtil.setSuccess(req, "Club updated successfully.");
            resp.sendRedirect(req.getContextPath() + "/clubs?id=" + clubId);
            return;
        }

        if (User.ROLE_CLUB_LEADER.equals(role)
                && existing.getLeaderId() != null
                && existing.getLeaderId().equals(userId)) {
            clubDAO.updateClubByLeader(clubId, userId, focusArea, description, activities);
            FlashUtil.setSuccess(req, "Club information updated successfully.");
            resp.sendRedirect(req.getContextPath() + "/clubs?id=" + clubId);
            return;
        }

        denyForbidden(req, resp);
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp, String role)
            throws SQLException, ServletException, IOException {
        if (!User.ROLE_ADMIN.equals(role)) {
            denyForbidden(req, resp);
            return;
        }

        Integer clubId = ValidationUtil.parsePositiveInt(req.getParameter("clubId"));
        if (clubId == null) {
            resp.sendRedirect(req.getContextPath() + "/clubs");
            return;
        }

        Club existing = clubDAO.findById(clubId, null);
        if (existing == null) {
            FlashUtil.setError(req, "Club not found.");
            resp.sendRedirect(req.getContextPath() + "/clubs");
            return;
        }

        if (Set.of("prakruthi", "spoorthi", "kruthi", "saheli").contains(existing.getClubSlug())) {
            FlashUtil.setError(req, "Core institutional clubs (Prakruthi, Spoorthi, Kruthi, Saheli) cannot be deleted.");
            resp.sendRedirect(req.getContextPath() + "/clubs?id=" + clubId);
            return;
        }

        if (clubDAO.hasLinkedRecords(clubId)) {
            FlashUtil.setError(req, "Cannot delete this club because it has linked memberships or events.");
            resp.sendRedirect(req.getContextPath() + "/clubs?id=" + clubId);
            return;
        }

        clubDAO.deleteClub(clubId);
        FlashUtil.setSuccess(req, "Club '" + existing.getClubName() + "' deleted successfully.");
        resp.sendRedirect(req.getContextPath() + "/clubs");
    }

    private void handleMembership(HttpServletRequest req, HttpServletResponse resp,
                                  Integer userId, boolean join) throws SQLException, IOException {
        Integer clubId = ValidationUtil.parsePositiveInt(req.getParameter("clubId"));
        if (clubId == null) {
            resp.sendRedirect(req.getContextPath() + "/clubs");
            return;
        }
        Club club = clubDAO.findById(clubId, userId);
        if (club == null) {
            FlashUtil.setError(req, "Club not found.");
            resp.sendRedirect(req.getContextPath() + "/clubs");
            return;
        }

        if (join) {
            clubDAO.joinClub(clubId, userId);
            FlashUtil.setSuccess(req, "You have joined " + club.getClubName() + "!");
        } else {
            clubDAO.leaveClub(clubId, userId);
            FlashUtil.setInfo(req, "You have left " + club.getClubName() + ".");
        }
        resp.sendRedirect(req.getContextPath() + "/clubs?id=" + clubId);
    }

    private void populateLeaderAndCoordinatorOptions(HttpServletRequest req) throws SQLException {
        req.setAttribute("facultyOptions", userDAO.findActiveUsersByRoles(User.ROLE_FACULTY, User.ROLE_ADMIN));
        req.setAttribute("leaderOptions", userDAO.findActiveUsersByRoles(User.ROLE_CLUB_LEADER));
    }

    private String sanitizeChoice(String raw, Set<String> allowed, String fallback) {
        String val = ValidationUtil.trimOrNull(raw);
        return (val != null && allowed.contains(val)) ? val : fallback;
    }

    private void denyForbidden(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        req.setAttribute("errorTitle", "403 — Access Denied");
        req.setAttribute("errorMessage", "You do not have permission to perform this action.");
        req.getRequestDispatcher("/error.jsp").forward(req, resp);
    }

    private void sendServerError(HttpServletRequest req, HttpServletResponse resp, String msg)
            throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        req.setAttribute("errorTitle", "Club Module Error");
        req.setAttribute("errorMessage", msg);
        req.getRequestDispatcher("/error.jsp").forward(req, resp);
    }
}
