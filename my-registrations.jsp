<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.campusflow.model.EventRegistration" %>
<%@ page import="com.campusflow.util.ViewHelper" %>
<%
    request.setAttribute("pageTitle", "My Registrations");
    request.setAttribute("activeNav", "registrations");

    @SuppressWarnings("unchecked")
    List<EventRegistration> registrations = (List<EventRegistration>) request.getAttribute("registrations");
%>
<%@ include file="/WEB-INF/fragments/head.jspf" %>
<%@ include file="/WEB-INF/fragments/navbar.jspf" %>

<main class="container py-4">
    <%@ include file="/WEB-INF/fragments/flash.jspf" %>

    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
        <div>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb small mb-1">
                    <li class="breadcrumb-item"><a href="<%= ctx %>/dashboard">Dashboard</a></li>
                    <li class="breadcrumb-item active" aria-current="page">My Registrations</li>
                </ol>
            </nav>
            <h1 class="h3 fw-bold mb-1">My Event Registrations &amp; Attendance</h1>
            <p class="text-body-secondary mb-0">
                View your active event registrations and verified participation status.
            </p>
        </div>
        <a href="<%= ctx %>/events" class="btn btn-cf-primary rounded-pill px-4">
            <i class="bi bi-calendar2-plus me-1"></i>Browse Events
        </a>
    </div>

    <section class="cf-card p-4 cf-animate-in">
        <% if (registrations != null && !registrations.isEmpty()) { %>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead>
                        <tr>
                            <th>Event</th>
                            <th>Date &amp; Venue</th>
                            <th>Registered On</th>
                            <th>Attendance Status</th>
                            <th class="text-end">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (EventRegistration reg : registrations) { %>
                            <tr>
                                <td>
                                    <a href="<%= ctx %>/events?id=<%= reg.getEventId() %>" class="fw-bold text-decoration-none">
                                        <%= ViewHelper.esc(reg.getEventName()) %>
                                    </a>
                                </td>
                                <td class="small text-body-secondary">
                                    <%= ViewHelper.esc(ViewHelper.formatDate(reg.getEventDate(), "TBD")) %>
                                    &middot;
                                    <%= reg.getVenue() != null ? ViewHelper.esc(reg.getVenue()) : "TBD" %>
                                </td>
                                <td class="small text-body-secondary">
                                    <%= ViewHelper.esc(ViewHelper.formatDateTime(reg.getRegistrationDate(), "-")) %>
                                </td>
                                <td>
                                    <% if ("PRESENT".equals(reg.getAttendanceStatus())) { %>
                                        <span class="badge rounded-pill badge-soft-success">
                                            <i class="bi bi-check-circle-fill me-1"></i>Present
                                        </span>
                                    <% } else if ("ABSENT".equals(reg.getAttendanceStatus())) { %>
                                        <span class="badge rounded-pill badge-soft-danger">
                                            <i class="bi bi-x-circle-fill me-1"></i>Absent
                                        </span>
                                    <% } else { %>
                                        <span class="badge rounded-pill badge-soft-secondary">
                                            <i class="bi bi-hourglass me-1"></i>Pending Event
                                        </span>
                                    <% } %>
                                </td>
                                <td class="text-end">
                                    <% if (reg.getAttendanceStatus() == null && "APPROVED".equals(reg.getEventStatus())) { %>
                                        <form method="post" action="<%= ctx %>/registrations" class="d-inline"
                                              data-confirm="Cancel registration for '<%= ViewHelper.esc(reg.getEventName()) %>'?">
                                            <input type="hidden" name="_csrf" value="<%= ViewHelper.csrf(request) %>">
                                            <input type="hidden" name="action" value="cancel">
                                            <input type="hidden" name="eventId" value="<%= reg.getEventId() %>">
                                            <input type="hidden" name="returnTo" value="my">
                                            <button type="submit" class="btn btn-sm btn-outline-danger rounded-pill px-3">
                                                Cancel
                                            </button>
                                        </form>
                                    <% } else { %>
                                        <a href="<%= ctx %>/events?id=<%= reg.getEventId() %>"
                                           class="btn btn-sm btn-outline-secondary rounded-pill px-3">View</a>
                                    <% } %>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        <% } else { %>
            <div class="text-center py-5 text-body-secondary">
                <i class="bi bi-ticket-perforated fs-1 d-block mb-2"></i>
                <h2 class="h5 fw-bold text-body">No Active Registrations Yet</h2>
                <p class="mb-3">Once you register for an upcoming club event or Lakshya, it will appear here.</p>
                <a href="<%= ctx %>/events" class="btn btn-outline-primary rounded-pill px-4">Explore Upcoming Events</a>
            </div>
        <% } %>
    </section>
</main>

<%@ include file="/WEB-INF/fragments/footer.jspf" %>
