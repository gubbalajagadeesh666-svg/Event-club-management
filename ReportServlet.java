package com.campusflow.servlet;

import com.campusflow.dao.EventDAO;
import com.campusflow.dao.ReportDAO;
import com.campusflow.model.User;
import com.campusflow.util.AuthUtil;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SQL-aggregated Reports controller (/reports).
 * Accessible to Administrators, Faculty Coordinators, and Club Leaders; restricted from Students.
 */
public class ReportServlet extends HttpServlet {

    private final ReportDAO reportDAO = new ReportDAO();
    private final EventDAO eventDAO = new EventDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer userId = AuthUtil.getUserId(req);
        String role = AuthUtil.getUserRole(req);

        if (User.ROLE_STUDENT.equals(role)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            req.setAttribute("errorTitle", "403 — Access Denied");
            req.setAttribute("errorMessage", "You do not have permission to access administrative reports.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
            return;
        }

        try {
            req.setAttribute("summary", reportDAO.getSystemSummary());
            req.setAttribute("clubReports", reportDAO.getParticipationByClub());
            req.setAttribute("events", eventDAO.findEvents("ALL", null, userId, role));
            req.getRequestDispatcher("/reports.jsp").forward(req, resp);
        } catch (SQLException ex) {
            getServletContext().log("Database error loading reports", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            req.setAttribute("errorTitle", "Reports Error");
            req.setAttribute("errorMessage", "Unable to compute report aggregations from MySQL.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }
}
