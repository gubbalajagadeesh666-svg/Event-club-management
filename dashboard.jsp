<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.util.List" %>
<%@ page import="com.campusflow.dao.ReportDAO" %>
<%@ page import="com.campusflow.model.Club" %>
<%@ page import="com.campusflow.model.Event" %>
<%@ page import="com.campusflow.model.EventRegistration" %>
<%@ page import="com.campusflow.model.User" %>
<%@ page import="com.campusflow.util.AuthUtil" %>
<%@ page import="com.campusflow.util.ViewHelper" %>
<%
    request.setAttribute("pageTitle", "Dashboard");
    request.setAttribute("activeNav", "dashboard");

    String role = AuthUtil.getUserRole(request);
    String userName = AuthUtil.getUserName(request);
    ReportDAO.SystemSummary summary = (ReportDAO.SystemSummary) request.getAttribute("summary");

    @SuppressWarnings("unchecked")
    List<Club> clubs = (List<Club>) request.getAttribute("clubs");
    @SuppressWarnings("unchecked")
    List<Club> assignedClubs = (List<Club>) request.getAttribute("assignedClubs");
    @SuppressWarnings("unchecked")
    List<Event> upcomingEvents = (List<Event>) request.getAttribute("upcomingEvents");
    @SuppressWarnings("unchecked")
    List<Event> pendingApprovals = (List<Event>) request.getAttribute("pendingApprovals");
    @SuppressWarnings("unchecked")
    List<EventRegistration> myRegistrations = (List<EventRegistration>) request.getAttribute("myRegistrations");

    Event lakshya = (Event) request.getAttribute("lakshya");
    LocalDate today = LocalDate.now();
    LocalDateTime now = LocalDateTime.now();
%>
<%@ include file="/WEB-INF/fragments/head.jspf" %>
<%@ include file="/WEB-INF/fragments/navbar.jspf" %>

<main class="container py-4">
    <%@ include file="/WEB-INF/fragments/flash.jspf" %>

    <!-- Welcome & Role Quick Actions Header -->
    <section class="cf-card p-4 mb-4 cf-animate-in">
        <div class="d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-3">
            <div>
                <div class="d-flex align-items-center gap-2 mb-1">
                    <span class="badge rounded-pill text-bg-primary"><%= ViewHelper.esc(ViewHelper.roleDisplay(role)) %> Workspace</span>
                </div>
                <h1 class="h3 fw-bold mb-1">Welcome, <%= ViewHelper.esc(userName) %>!</h1>
                <p class="text-body-secondary mb-0">
                    <% if (User.ROLE_ADMIN.equals(role)) { %>
                        Manage campus clubs, approve event proposals, oversee Lakshya, and assign staff &amp; leader roles.
                    <% } else if (User.ROLE_FACULTY.equals(role)) { %>
                        Monitor your assigned clubs, review student event proposals, and track attendance &amp; reports.
                    <% } else if (User.ROLE_CLUB_LEADER.equals(role)) { %>
                        Update your assigned club profile, submit new event proposals, and mark participant attendance.
                    <% } else { %>
                        Explore campus clubs, register for upcoming events, and stay updated on Lakshya.
                    <% } %>
                </p>
            </div>

            <div class="d-flex flex-wrap gap-2">
                <% if (User.ROLE_ADMIN.equals(role)) { %>
                    <a href="<%= ctx %>/clubs" class="btn btn-cf-primary rounded-pill px-3">
                        <i class="bi bi-plus-circle me-1"></i>Manage Clubs
                    </a>
                    <a href="<%= ctx %>/events" class="btn btn-outline-primary rounded-pill px-3">
                        <i class="bi bi-calendar-plus me-1"></i>Manage Events
                    </a>
                    <a href="<%= ctx %>/admin/users" class="btn btn-outline-secondary rounded-pill px-3">
                        <i class="bi bi-person-gear me-1"></i>Users &amp; Roles
                    </a>
                <% } else if (User.ROLE_FACULTY.equals(role)) { %>
                    <a href="<%= ctx %>/events?status=PENDING_APPROVAL" class="btn btn-cf-primary rounded-pill px-3">
                        <i class="bi bi-clipboard2-check me-1"></i>Review Proposals
                    </a>
                    <a href="<%= ctx %>/reports" class="btn btn-outline-primary rounded-pill px-3">
                        <i class="bi bi-bar-chart-line me-1"></i>View Reports
                    </a>
                <% } else if (User.ROLE_CLUB_LEADER.equals(role)) { %>
                    <a href="<%= ctx %>/events" class="btn btn-cf-primary rounded-pill px-3">
                        <i class="bi bi-send-plus me-1"></i>Submit Event Proposal
                    </a>
                    <a href="<%= ctx %>/clubs" class="btn btn-outline-primary rounded-pill px-3">
                        <i class="bi bi-people me-1"></i>My Club
                    </a>
                <% } else { %>
                    <a href="<%= ctx %>/events" class="btn btn-cf-primary rounded-pill px-3">
                        <i class="bi bi-calendar2-check me-1"></i>Browse Events
                    </a>
                    <a href="<%= ctx %>/registrations" class="btn btn-outline-primary rounded-pill px-3">
                        <i class="bi bi-ticket-perforated me-1"></i>My Registrations
                    </a>
                <% } %>
            </div>
        </div>
    </section>

    <!-- Summary Cards (Real MySQL Aggregates) -->
    <section class="row g-3 mb-4">
        <div class="col-sm-6 col-xl-3">
            <div class="cf-card cf-stat-card p-3 h-100 cf-animate-in">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="small text-body-secondary fw-medium">Campus Clubs</div>
                        <div class="fs-2 fw-bold"><%= summary != null ? summary.totalClubs() : 0 %></div>
                    </div>
                    <div class="cf-stat-icon bg-primary bg-opacity-10 text-primary">
                        <i class="bi bi-people-fill"></i>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-sm-6 col-xl-3">
            <div class="cf-card cf-stat-card p-3 h-100 cf-animate-in cf-delay-1">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="small text-body-secondary fw-medium">Upcoming Open Events</div>
                        <div class="fs-2 fw-bold"><%= summary != null ? summary.upcomingEvents() : 0 %></div>
                    </div>
                    <div class="cf-stat-icon bg-success bg-opacity-10 text-success">
                        <i class="bi bi-calendar2-event-fill"></i>
                    </div>
                </div>
            </div>
        </div>

        <% if (User.ROLE_ADMIN.equals(role) || User.ROLE_FACULTY.equals(role)) { %>
            <div class="col-sm-6 col-xl-3">
                <div class="cf-card cf-stat-card p-3 h-100 cf-animate-in cf-delay-2">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="small text-body-secondary fw-medium">Pending Proposals</div>
                            <div class="fs-2 fw-bold"><%= pendingApprovals != null ? pendingApprovals.size() : 0 %></div>
                        </div>
                        <div class="cf-stat-icon bg-warning bg-opacity-10 text-warning">
                            <i class="bi bi-hourglass-split"></i>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="cf-card cf-stat-card p-3 h-100 cf-animate-in cf-delay-3">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="small text-body-secondary fw-medium">Active Registrations</div>
                            <div class="fs-2 fw-bold"><%= summary != null ? summary.totalRegistrations() : 0 %></div>
                        </div>
                        <div class="cf-stat-icon bg-info bg-opacity-10 text-info">
                            <i class="bi bi-person-check-fill"></i>
                        </div>
                    </div>
                </div>
            </div>
        <% } else { %>
            <div class="col-sm-6 col-xl-3">
                <div class="cf-card cf-stat-card p-3 h-100 cf-animate-in cf-delay-2">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="small text-body-secondary fw-medium">My Event Registrations</div>
                            <div class="fs-2 fw-bold"><%= myRegistrations != null ? myRegistrations.size() : 0 %></div>
                        </div>
                        <div class="cf-stat-icon bg-info bg-opacity-10 text-info">
                            <i class="bi bi-ticket-perforated-fill"></i>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="cf-card cf-stat-card p-3 h-100 cf-animate-in cf-delay-3">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="small text-body-secondary fw-medium">Total Campus Registrations</div>
                            <div class="fs-2 fw-bold"><%= summary != null ? summary.totalRegistrations() : 0 %></div>
                        </div>
                        <div class="cf-stat-icon bg-warning bg-opacity-10 text-warning">
                            <i class="bi bi-award-fill"></i>
                        </div>
                    </div>
                </div>
            </div>
        <% } %>
    </section>

    <!-- Assigned Club Notice for Faculty Coordinator / Club Leader -->
    <% if ((User.ROLE_FACULTY.equals(role) || User.ROLE_CLUB_LEADER.equals(role))
            && assignedClubs != null && !assignedClubs.isEmpty()) { %>
        <section class="cf-card p-4 mb-4 border-primary border-opacity-25 cf-animate-in">
            <h2 class="h6 text-uppercase fw-bold text-primary mb-3">
                <i class="bi bi-person-badge-fill me-1"></i>Your Assigned Club Responsibilities
            </h2>
            <div class="d-flex flex-wrap gap-2">
                <% for (Club ac : assignedClubs) { %>
                    <a href="<%= ctx %>/clubs?id=<%= ac.getClubId() %>"
                       class="btn btn-outline-primary rounded-pill px-3">
                        <i class="bi <%= ViewHelper.esc(ac.getIconClass()) %> me-1"></i>
                        <%= ViewHelper.esc(ac.getClubName()) %>
                        <span class="badge text-bg-secondary ms-1"><%= ac.getMemberCount() %> members</span>
                    </a>
                <% } %>
            </div>
        </section>
    <% } %>

    <!-- Pending Event Proposals Queue (Admin & Assigned Faculty Coordinator) -->
    <% if ((User.ROLE_ADMIN.equals(role) || User.ROLE_FACULTY.equals(role))
            && pendingApprovals != null && !pendingApprovals.isEmpty()) { %>
        <section class="cf-card p-4 mb-4 border-warning border-opacity-50 cf-animate-in">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h2 class="h5 fw-bold mb-0">
                    <i class="bi bi-hourglass-split text-warning me-2"></i>Event Proposals Awaiting Review
                </h2>
                <span class="badge badge-soft-warning rounded-pill px-3 py-2">
                    <%= pendingApprovals.size() %> Pending
                </span>
            </div>
            <div class="table-responsive">
                <table class="table align-middle mb-0">
                    <thead>
                        <tr>
                            <th>Proposal</th>
                            <th>Proposed Date &amp; Venue</th>
                            <th>Submitted By</th>
                            <th class="text-end">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Event pe : pendingApprovals) { %>
                            <tr>
                                <td class="fw-semibold"><%= ViewHelper.esc(pe.getEventName()) %></td>
                                <td class="small text-body-secondary">
                                    <%= ViewHelper.esc(ViewHelper.formatDate(pe.getEventDate(), "TBD")) %>
                                    &middot; <%= pe.getVenue() != null ? ViewHelper.esc(pe.getVenue()) : "TBD" %>
                                </td>
                                <td class="small"><%= pe.getCreatedByName() != null ? ViewHelper.esc(pe.getCreatedByName()) : "Club Leader" %></td>
                                <td class="text-end">
                                    <a href="<%= ctx %>/events?id=<%= pe.getEventId() %>"
                                       class="btn btn-sm btn-warning rounded-pill px-3 fw-semibold text-dark">
                                        Review Proposal
                                    </a>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </section>
    <% } %>

    <div class="row g-4 mb-4">
        <!-- Upcoming Events Column -->
        <div class="col-lg-7">
            <section class="cf-card p-4 h-100 cf-animate-in">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h2 class="h5 fw-bold mb-0"><i class="bi bi-calendar2-week text-primary me-2"></i>Upcoming Events</h2>
                    <a href="<%= ctx %>/events" class="btn btn-sm btn-outline-primary rounded-pill px-3">All Events</a>
                </div>

                <% if (upcomingEvents != null && !upcomingEvents.isEmpty()) { %>
                    <div class="d-grid gap-3">
                        <% for (Event ev : upcomingEvents) {
                            boolean open = ev.isRegistrationOpen(today, now);
                        %>
                            <div class="p-3 rounded-3 border bg-body-tertiary d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-3">
                                <div>
                                    <div class="d-flex flex-wrap align-items-center gap-2 mb-1">
                                        <span class="fw-bold"><%= ViewHelper.esc(ev.getEventName()) %></span>
                                        <span class="badge rounded-pill <%= ev.getStatusBadgeClass() %>">
                                            <i class="bi <%= ev.getStatusIconClass() %> me-1"></i><%= ViewHelper.esc(ev.getStatusDisplayName()) %>
                                        </span>
                                        <% if (ev.isUserRegistered()) { %>
                                            <span class="badge rounded-pill badge-soft-info">
                                                <i class="bi bi-check2-all me-1"></i>Registered
                                            </span>
                                        <% } %>
                                    </div>
                                    <div class="small text-body-secondary">
                                        <i class="bi bi-calendar3 me-1"></i><%= ViewHelper.esc(ViewHelper.formatDate(ev.getEventDate(), "TBD")) %>
                                        &nbsp;&middot;&nbsp;
                                        <i class="bi bi-geo-alt me-1"></i><%= ev.getVenue() != null ? ViewHelper.esc(ev.getVenue()) : "TBD" %>
                                        &nbsp;&middot;&nbsp;
                                        <i class="bi bi-people me-1"></i><%= ev.getRegisteredCount() %><%= ev.getCapacity() != null ? (" / " + ev.getCapacity()) : "" %>
                                    </div>
                                </div>
                                <a href="<%= ctx %>/<%= ev.isFlagship() ? "lakshya" : ("events?id=" + ev.getEventId()) %>"
                                   class="btn btn-sm <%= open && !ev.isUserRegistered() ? "btn-cf-primary" : "btn-outline-secondary" %> rounded-pill px-3 flex-shrink-0">
                                    View Details
                                </a>
                            </div>
                        <% } %>
                    </div>
                <% } else { %>
                    <div class="text-center py-4 text-body-secondary">
                        <i class="bi bi-calendar2-x fs-2 d-block mb-2"></i>
                        <p class="mb-2">No upcoming approved events are currently scheduled.</p>
                        <% if (User.ROLE_ADMIN.equals(role) || User.ROLE_CLUB_LEADER.equals(role)) { %>
                            <a href="<%= ctx %>/events" class="btn btn-sm btn-outline-primary rounded-pill px-3">
                                Schedule / Propose an Event
                            </a>
                        <% } %>
                    </div>
                <% } %>
            </section>
        </div>

        <!-- Lakshya Flagship Highlight Column -->
        <div class="col-lg-5">
            <% if (lakshya != null) { %>
                <section class="lakshya-spotlight p-4 h-100 d-flex flex-column justify-content-between cf-animate-in">
                    <div>
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <span class="badge rounded-pill bg-warning text-dark fw-bold px-3 py-2">
                                <i class="bi bi-stars me-1"></i>Flagship Highlight
                            </span>
                            <span class="badge rounded-pill bg-white bg-opacity-10 border border-light border-opacity-25">
                                <%= ViewHelper.esc(lakshya.getStatusDisplayName()) %>
                            </span>
                        </div>
                        <h2 class="h3 fw-bold mb-2"><%= ViewHelper.esc(lakshya.getEventName()) %></h2>
                        <p class="small text-white-50 mb-3"><%= ViewHelper.esc(lakshya.getDescription()) %></p>

                        <div class="lakshya-glass-tile mb-3 small">
                            <div class="d-flex justify-content-between py-1 border-bottom border-light border-opacity-10">
                                <span class="text-white-50">Official Date</span>
                                <span class="fw-semibold"><%= ViewHelper.esc(ViewHelper.formatDate(lakshya.getEventDate(), "To be announced")) %></span>
                            </div>
                            <div class="d-flex justify-content-between py-1 border-bottom border-light border-opacity-10">
                                <span class="text-white-50">Venue</span>
                                <span class="fw-semibold"><%= lakshya.getVenue() != null ? ViewHelper.esc(lakshya.getVenue()) : "To be announced" %></span>
                            </div>
                            <div class="d-flex justify-content-between pt-1">
                                <span class="text-white-50">Registered Students</span>
                                <span class="fw-bold text-warning"><%= lakshya.getRegisteredCount() %></span>
                            </div>
                        </div>
                    </div>

                    <a href="<%= ctx %>/lakshya" class="btn btn-warning rounded-pill fw-semibold text-dark w-100">
                        <% if (User.ROLE_ADMIN.equals(role) && Event.STATUS_SCHEDULE_PENDING.equals(lakshya.getStatus())) { %>
                            <i class="bi bi-pencil-square me-1"></i>Complete Official Lakshya Details
                        <% } else { %>
                            <i class="bi bi-arrow-right-circle-fill me-1"></i>View Lakshya Hub
                        <% } %>
                    </a>
                </section>
            <% } %>
        </div>
    </div>

    <!-- Campus Clubs Overview -->
    <section class="cf-card p-4 cf-animate-in">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h2 class="h5 fw-bold mb-0"><i class="bi bi-compass text-primary me-2"></i>Campus Clubs Directory</h2>
            <a href="<%= ctx %>/clubs" class="btn btn-sm btn-outline-primary rounded-pill px-3">Explore All</a>
        </div>

        <div class="row g-3">
            <% if (clubs != null && !clubs.isEmpty()) {
                for (Club club : clubs) { %>
                <div class="col-md-6 col-xl-3">
                    <div class="p-3 rounded-3 border h-100 d-flex flex-column club-accent-<%= ViewHelper.esc(club.getAccentTheme()) %>">
                        <div class="d-flex align-items-center gap-3 mb-2">
                            <div class="club-icon-box" style="width:42px;height:42px;font-size:1.2rem;">
                                <i class="bi <%= ViewHelper.esc(club.getIconClass()) %>"></i>
                            </div>
                            <div>
                                <h3 class="h6 fw-bold mb-0"><%= ViewHelper.esc(club.getClubName()) %></h3>
                                <span class="small text-body-secondary"><%= club.getMemberCount() %> member(s)</span>
                            </div>
                        </div>
                        <p class="small text-body-secondary flex-grow-1 mb-3"><%= ViewHelper.esc(club.getFocusArea()) %></p>
                        <a href="<%= ctx %>/clubs?id=<%= club.getClubId() %>"
                           class="btn btn-sm btn-outline-primary rounded-pill w-100">
                            Open Club
                        </a>
                    </div>
                </div>
            <%  }
               } %>
        </div>
    </section>
</main>

<%@ include file="/WEB-INF/fragments/footer.jspf" %>
