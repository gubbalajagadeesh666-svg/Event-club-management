<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.campusflow.dao.ReportDAO" %>
<%@ page import="com.campusflow.model.ClubReportRow" %>
<%@ page import="com.campusflow.model.Event" %>
<%@ page import="com.campusflow.util.ViewHelper" %>
<%
    request.setAttribute("pageTitle", "Reports & Statistics");
    request.setAttribute("activeNav", "reports");

    ReportDAO.SystemSummary summary = (ReportDAO.SystemSummary) request.getAttribute("summary");
    @SuppressWarnings("unchecked")
    List<ClubReportRow> clubReports = (List<ClubReportRow>) request.getAttribute("clubReports");
    @SuppressWarnings("unchecked")
    List<Event> events = (List<Event>) request.getAttribute("events");
%>
<%@ include file="/WEB-INF/fragments/head.jspf" %>
<%@ include file="/WEB-INF/fragments/navbar.jspf" %>

<main class="container py-4">
    <%@ include file="/WEB-INF/fragments/flash.jspf" %>

    <div class="mb-4">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb small mb-1">
                <li class="breadcrumb-item"><a href="<%= ctx %>/dashboard">Dashboard</a></li>
                <li class="breadcrumb-item active" aria-current="page">Reports</li>
            </ol>
        </nav>
        <h1 class="h3 fw-bold mb-1">Participation &amp; Activity Reports</h1>
        <p class="text-body-secondary mb-0">
            Live SQL-aggregated counts directly from <code>campusflow_db</code>.
        </p>
    </div>

    <!-- Aggregate KPI Row -->
    <section class="row g-3 mb-4">
        <div class="col-6 col-lg-2">
            <div class="cf-card p-3 text-center h-100">
                <div class="fs-3 fw-bold text-primary"><%= summary != null ? summary.totalClubs() : 0 %></div>
                <div class="small text-body-secondary">Total Clubs</div>
            </div>
        </div>
        <div class="col-6 col-lg-2">
            <div class="cf-card p-3 text-center h-100">
                <div class="fs-3 fw-bold text-primary"><%= summary != null ? summary.totalEvents() : 0 %></div>
                <div class="small text-body-secondary">Total Events</div>
            </div>
        </div>
        <div class="col-6 col-lg-2">
            <div class="cf-card p-3 text-center h-100">
                <div class="fs-3 fw-bold text-success"><%= summary != null ? summary.upcomingEvents() : 0 %></div>
                <div class="small text-body-secondary">Upcoming Open</div>
            </div>
        </div>
        <div class="col-6 col-lg-2">
            <div class="cf-card p-3 text-center h-100">
                <div class="fs-3 fw-bold text-warning"><%= summary != null ? summary.pendingApprovals() : 0 %></div>
                <div class="small text-body-secondary">Pending Approval</div>
            </div>
        </div>
        <div class="col-6 col-lg-2">
            <div class="cf-card p-3 text-center h-100">
                <div class="fs-3 fw-bold text-info"><%= summary != null ? summary.totalRegistrations() : 0 %></div>
                <div class="small text-body-secondary">Registrations</div>
            </div>
        </div>
        <div class="col-6 col-lg-2">
            <div class="cf-card p-3 text-center h-100">
                <div class="fs-3 fw-bold text-success"><%= summary != null ? summary.totalPresentAttendance() : 0 %></div>
                <div class="small text-body-secondary">Marked Present</div>
            </div>
        </div>
    </section>

    <!-- Participation by Club -->
    <section class="cf-card p-4 mb-4 cf-animate-in">
        <h2 class="h5 fw-bold mb-3">
            <i class="bi bi-people-fill text-primary me-2"></i>Participation Summary by Club
        </h2>
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead>
                    <tr>
                        <th>Club</th>
                        <th class="text-center">Active Members</th>
                        <th class="text-center">Associated Events</th>
                        <th class="text-center">Approved / Completed Events</th>
                        <th class="text-center">Event Registrations</th>
                        <th class="text-center">Present Attendance</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (clubReports != null && !clubReports.isEmpty()) {
                        for (ClubReportRow row : clubReports) { %>
                        <tr class="club-accent-<%= ViewHelper.esc(row.getAccentTheme()) %>">
                            <td>
                                <a href="<%= ctx %>/clubs?id=<%= row.getClubId() %>"
                                   class="d-inline-flex align-items-center gap-2 fw-bold text-decoration-none">
                                    <span class="club-icon-box" style="width:34px;height:34px;font-size:1rem;">
                                        <i class="bi <%= ViewHelper.esc(row.getIconClass()) %>"></i>
                                    </span>
                                    <span><%= ViewHelper.esc(row.getClubName()) %></span>
                                </a>
                            </td>
                            <td class="text-center fw-semibold"><%= row.getActiveMembers() %></td>
                            <td class="text-center"><%= row.getTotalEvents() %></td>
                            <td class="text-center"><%= row.getApprovedEvents() %></td>
                            <td class="text-center fw-semibold text-primary"><%= row.getTotalRegistrations() %></td>
                            <td class="text-center fw-semibold text-success"><%= row.getPresentAttendance() %></td>
                        </tr>
                    <%  }
                       } %>
                </tbody>
            </table>
        </div>
    </section>

    <!-- Event-Level Registration & Attendance Breakdown -->
    <section class="cf-card p-4 cf-animate-in">
        <h2 class="h5 fw-bold mb-3">
            <i class="bi bi-calendar2-check-fill text-primary me-2"></i>Event Registration &amp; Attendance Breakdown
        </h2>
        <% if (events != null && !events.isEmpty()) { %>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead>
                        <tr>
                            <th>Event</th>
                            <th>Date</th>
                            <th>Status</th>
                            <th class="text-center">Registered</th>
                            <th class="text-center">Capacity</th>
                            <th class="text-center">Marked Present</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Event ev : events) { %>
                            <tr>
                                <td>
                                    <a href="<%= ctx %>/<%= ev.isFlagship() ? "lakshya" : ("events?id=" + ev.getEventId()) %>"
                                       class="fw-semibold text-decoration-none">
                                        <%= ViewHelper.esc(ev.getEventName()) %>
                                    </a>
                                </td>
                                <td class="small text-body-secondary"><%= ViewHelper.esc(ViewHelper.formatDate(ev.getEventDate(), "TBD")) %></td>
                                <td>
                                    <span class="badge rounded-pill <%= ev.getStatusBadgeClass() %>">
                                        <%= ViewHelper.esc(ev.getStatusDisplayName()) %>
                                    </span>
                                </td>
                                <td class="text-center fw-semibold"><%= ev.getRegisteredCount() %></td>
                                <td class="text-center text-body-secondary"><%= ev.getCapacity() != null ? ev.getCapacity() : "Unlimited" %></td>
                                <td class="text-center fw-semibold text-success"><%= ev.getPresentCount() %></td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        <% } else { %>
            <p class="text-body-secondary mb-0">No events recorded yet.</p>
        <% } %>
    </section>
</main>

<%@ include file="/WEB-INF/fragments/footer.jspf" %>
