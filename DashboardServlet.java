package com.campusflow.servlet;

import com.campusflow.dao.ClubDAO;
import com.campusflow.dao.EventDAO;
import com.campusflow.dao.RegistrationDAO;
import com.campusflow.dao.ReportDAO;
import com.campusflow.model.User;
import com.campusflow.util.AuthUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Role-aware Dashboard controller (/dashboard).
 * Retrieves real counts, clubs, Lakshya spotlight, upcoming events, and role-specific queues.
 */
public class DashboardServlet extends HttpServlet {

    private final ReportDAO reportDAO = new ReportDAO();
    private final ClubDAO clubDAO = new ClubDAO();
    private final EventDAO eventDAO = new EventDAO();
    private final RegistrationDAO registrationDAO = new RegistrationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer userId = AuthUtil.getUserId(req);
        String role = AuthUtil.getUserRole(req);

        try {
            req.setAttribute("summary", reportDAO.getSystemSummary());
            req.setAttribute("clubs", clubDAO.findAllClubs(userId));
            req.setAttribute("lakshya", eventDAO.findLakshya(userId));
            req.setAttribute("upcomingEvents", eventDAO.findUpcomingEvents(5, userId));

            if (User.ROLE_ADMIN.equals(role) || User.ROLE_FACULTY.equals(role)) {
                req.setAttribute("pendingApprovals", eventDAO.findPendingApprovalsForReviewer(userId, role));
            } else {
                req.setAttribute("pendingApprovals", List.of());
            }

            if (User.ROLE_FACULTY.equals(role)) {
                req.setAttribute("assignedClubs", clubDAO.findByCoordinator(userId));
            } else if (User.ROLE_CLUB_LEADER.equals(role)) {
                req.setAttribute("assignedClubs", clubDAO.findByLeader(userId));
            } else {
                req.setAttribute("assignedClubs", List.of());
            }

            req.setAttribute("myRegistrations", registrationDAO.findByStudent(userId));
            req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
        } catch (SQLException ex) {
            getServletContext().log("Database error loading dashboard", ex);
            req.setAttribute("errorTitle", "Dashboard Error");
            req.setAttribute("errorMessage", "Unable to load dashboard data from MySQL.");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }
}
