package com.campusflow.servlet;

import com.campusflow.dao.ClubDAO;
import com.campusflow.dao.EventDAO;
import com.campusflow.dao.ReportDAO;
import com.campusflow.dao.UserDAO;
import com.campusflow.util.AuthUtil;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Public landing page Servlet (/home).
 * Loads dynamic club and Lakshya highlights from MySQL so index.jsp contains zero SQL.
 */
public class HomeServlet extends HttpServlet {

    private final ClubDAO clubDAO = new ClubDAO();
    private final EventDAO eventDAO = new EventDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ReportDAO reportDAO = new ReportDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer userId = AuthUtil.getUserId(req);
        try {
            req.setAttribute("clubs", clubDAO.findAllClubs(userId));
            req.setAttribute("lakshya", eventDAO.findLakshya(userId));
            req.setAttribute("upcomingEvents", eventDAO.findUpcomingEvents(3, userId));
            req.setAttribute("summary", reportDAO.getSystemSummary());
            req.setAttribute("needsInitialSetup", userDAO.countAdmins() == 0);
            req.setAttribute("dbAvailable", true);
        } catch (SQLException ex) {
            getServletContext().log("Database error on HomeServlet", ex);
            req.setAttribute("dbAvailable", false);
            req.setAttribute("dbErrorMessage", "Unable to connect to the database. Please verify MySQL is running.");
        }
        req.getRequestDispatcher("/index.jsp").forward(req, resp);
    }
}
