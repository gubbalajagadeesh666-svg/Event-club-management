<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.util.List" %>
<%@ page import="com.campusflow.model.Club" %>
<%@ page import="com.campusflow.model.Event" %>
<%@ page import="com.campusflow.model.User" %>
<%@ page import="com.campusflow.util.AuthUtil" %>
<%@ page import="com.campusflow.util.ViewHelper" %>
<%
    request.setAttribute("pageTitle", "Events");
    request.setAttribute("activeNav", "events");

    String role = AuthUtil.getUserRole(request);
    boolean isAdmin = User.ROLE_ADMIN.equals(role);
    boolean isFaculty = User.ROLE_FACULTY.equals(role);
    boolean isLeader = User.ROLE_CLUB_LEADER.equals(role);

    String selectedStatus = (String) request.getAttribute("selectedStatus");
    Integer selectedClubId = (Integer) request.getAttribute("selectedClubId");

    @SuppressWarnings("unchecked")
    List<Event> events = (List<Event>) request.getAttribute("events");
    @SuppressWarnings("unchecked")
    List<Club> allClubs = (List<Club>) request.getAttribute("allClubs");
    @SuppressWarnings("unchecked")
    List<Club> creatableClubs = (List<Club>) request.getAttribute("creatableClubs");

    boolean canCreateOrPropose = isAdmin || (isLeader && creatableClubs != null && !creatableClubs.isEmpty());
    LocalDate today = LocalDate.now();
    LocalDateTime now = LocalDateTime.now();
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
                    <li class="breadcrumb-item active" aria-current="page">Events</li>
                </ol>
            </nav>
            <h1 class="h3 fw-bold mb-1">Campus &amp; Club Events</h1>
            <p class="text-body-secondary mb-0">
                Browse upcoming workshops, competitions, cultural programs, and flagship celebrations.
            </p>
        </div>

        <% if (canCreateOrPropose) { %>
            <button type="button" class="btn btn-cf-primary rounded-pill px-4"
                    data-bs-toggle="modal" data-bs-target="#createEventModal">
                <i class="bi bi-calendar-plus me-1"></i>
                <%= isAdmin ? "Create Event" : "Submit Event Proposal" %>
            </button>
        <% } else if (isLeader) { %>
            <span class="badge badge-soft-warning rounded-pill px-3 py-2">
                <i class="bi bi-info-circle me-1"></i>Ask an Admin to assign your club before submitting proposals
            </span>
        <% } %>
    </div>

    <!-- Filter Bar -->
    <section class="cf-card p-3 mb-4">
        <form method="get" action="<%= ctx %>/events" class="row g-2 align-items-end">
            <div class="col-sm-5 col-md-4">
                <label for="filterClub" class="form-label small fw-semibold mb-1">Filter by Club</label>
                <select class="form-select form-select-sm" id="filterClub" name="clubId">
                    <option value="">All Clubs</option>
                    <% if (allClubs != null) {
                        for (Club c : allClubs) {
                            boolean sel = selectedClubId != null && selectedClubId.equals(c.getClubId());
                    %>
                        <option value="<%= c.getClubId() %>" <%= sel ? "selected" : "" %>><%= ViewHelper.esc(c.getClubName()) %></option>
                    <%  }
                       } %>
                </select>
            </div>

            <div class="col-sm-5 col-md-4">
                <label for="filterStatus" class="form-label small fw-semibold mb-1">Filter by Status</label>
                <select class="form-select form-select-sm" id="filterStatus" name="status">
                    <option value="ALL" <%= "ALL".equals(selectedStatus) ? "selected" : "" %>>All Visible Statuses</option>
                    <option value="APPROVED" <%= "APPROVED".equals(selectedStatus) ? "selected" : "" %>>Approved / Open</option>
                    <option value="SCHEDULE_PENDING" <%= "SCHEDULE_PENDING".equals(selectedStatus) ? "selected" : "" %>>Schedule Pending</option>
                    <option value="COMPLETED" <%= "COMPLETED".equals(selectedStatus) ? "selected" : "" %>>Completed</option>
                    <% if (isAdmin || isFaculty || isLeader) { %>
                        <option value="PENDING_APPROVAL" <%= "PENDING_APPROVAL".equals(selectedStatus) ? "selected" : "" %>>Pending Approval</option>
                        <option value="REJECTED" <%= "REJECTED".equals(selectedStatus) ? "selected" : "" %>>Rejected</option>
                        <option value="CANCELLED" <%= "CANCELLED".equals(selectedStatus) ? "selected" : "" %>>Cancelled</option>
                    <% } %>
                </select>
            </div>

            <div class="col-sm-2 col-md-4 d-flex gap-2">
                <button type="submit" class="btn btn-sm btn-primary rounded-pill px-3">
                    <i class="bi bi-funnel-fill me-1"></i>Apply
                </button>
                <a href="<%= ctx %>/events" class="btn btn-sm btn-outline-secondary rounded-pill px-3">Reset</a>
            </div>
        </form>
    </section>

    <!-- Event Cards Grid -->
    <div class="row g-4">
        <% if (events != null && !events.isEmpty()) {
            for (Event ev : events) {
                boolean open = ev.isRegistrationOpen(today, now);
        %>
            <div class="col-md-6 col-xl-4">
                <article class="cf-card cf-card-hover h-100 d-flex flex-column p-4 cf-animate-in">
                    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-2">
                        <span class="badge rounded-pill <%= ev.getStatusBadgeClass() %>">
                            <i class="bi <%= ev.getStatusIconClass() %> me-1"></i><%= ViewHelper.esc(ev.getStatusDisplayName()) %>
                        </span>
                        <% if (ev.isFlagship()) { %>
                            <span class="badge rounded-pill bg-warning text-dark fw-semibold">
                                <i class="bi bi-stars me-1"></i>Flagship
                            </span>
                        <% } else if (ev.isUserRegistered()) { %>
                            <span class="badge rounded-pill badge-soft-info">
                                <i class="bi bi-check2-all me-1"></i>Registered
                            </span>
                        <% } %>
                    </div>

                    <h2 class="h5 fw-bold mb-2"><%= ViewHelper.esc(ev.getEventName()) %></h2>
                    <p class="small text-body-secondary mb-3 flex-grow-1"><%= ViewHelper.esc(ev.getDescription()) %></p>

                    <% if (!ev.getAssociatedClubs().isEmpty()) { %>
                        <div class="d-flex flex-wrap gap-1 mb-3">
                            <% for (Club ac : ev.getAssociatedClubs()) { %>
                                <span class="badge rounded-pill bg-body-tertiary text-body border">
                                    <i class="bi <%= ViewHelper.esc(ac.getIconClass()) %> me-1 text-primary"></i>
                                    <%= ViewHelper.esc(ac.getClubName()) %>
                                </span>
                            <% } %>
                        </div>
                    <% } %>

                    <div class="p-3 rounded-3 bg-body-tertiary small mb-3 d-grid gap-1">
                        <div>
                            <i class="bi bi-calendar3 text-primary me-2"></i>
                            <strong><%= ViewHelper.esc(ViewHelper.formatDate(ev.getEventDate(), "To be announced")) %></strong>
                            <% if (ev.getStartTime() != null) { %>
                                &middot; <%= ViewHelper.esc(ViewHelper.formatTime(ev.getStartTime(), "")) %>
                            <% } %>
                        </div>
                        <div>
                            <i class="bi bi-geo-alt text-primary me-2"></i>
                            <%= ev.getVenue() != null ? ViewHelper.esc(ev.getVenue()) : "Venue to be announced" %>
                        </div>
                        <div>
                            <i class="bi bi-people text-primary me-2"></i>
                            <%= ev.getRegisteredCount() %> registered
                            <% if (ev.getCapacity() != null) { %>
                                (<%= ev.getRemainingSeats() %> of <%= ev.getCapacity() %> seats left)
                            <% } %>
                        </div>
                    </div>

                    <a href="<%= ctx %>/<%= ev.isFlagship() ? "lakshya" : ("events?id=" + ev.getEventId()) %>"
                       class="btn <%= open && !ev.isUserRegistered() ? "btn-cf-primary" : "btn-outline-primary" %> rounded-pill w-100">
                        <%= ev.isFlagship() ? "Open Lakshya Hub" : "View Event Details" %>
                    </a>
                </article>
            </div>
        <%  }
           } else { %>
            <div class="col-12">
                <div class="cf-card p-5 text-center text-body-secondary">
                    <i class="bi bi-calendar2-x fs-1 d-block mb-2"></i>
                    <h2 class="h5 fw-bold text-body">No Matching Events Found</h2>
                    <p class="mb-0">Try clearing your filters or create/propose a new club event.</p>
                </div>
            </div>
        <% } %>
    </div>

    <!-- Create Event / Submit Proposal Modal -->
    <% if (canCreateOrPropose) { %>
        <div class="modal fade" id="createEventModal" tabindex="-1" aria-labelledby="createEventModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-lg modal-dialog-centered">
                <div class="modal-content rounded-4 border-0 shadow">
                    <form method="post" action="<%= ctx %>/events" class="needs-validation" novalidate>
                        <input type="hidden" name="_csrf" value="<%= ViewHelper.csrf(request) %>">
                        <input type="hidden" name="action" value="create">

                        <div class="modal-header border-bottom-0 pb-0">
                            <h2 class="modal-title h5 fw-bold" id="createEventModalLabel">
                                <i class="bi bi-calendar-plus text-primary me-2"></i>
                                <%= isAdmin ? "Create &amp; Publish Event" : "Submit Club Event Proposal" %>
                            </h2>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>

                        <div class="modal-body">
                            <% if (isLeader) { %>
                                <div class="alert alert-info small py-2 mb-3">
                                    <i class="bi bi-info-circle-fill me-1"></i>
                                    Proposals submitted by Club Leaders are recorded as <strong>Pending Approval</strong>
                                    until reviewed by your Faculty Coordinator or Administrator.
                                </div>
                            <% } %>

                            <div class="row g-3">
                                <div class="col-md-8">
                                    <label for="evName" class="form-label fw-medium">Event Name</label>
                                    <input type="text" class="form-control" id="evName" name="eventName"
                                           maxlength="150" required placeholder="e.g., Campus Tree Plantation Drive">
                                    <div class="invalid-feedback">Event name is required.</div>
                                </div>

                                <div class="col-md-4">
                                    <label for="evCap" class="form-label fw-medium">Capacity (Optional)</label>
                                    <input type="number" class="form-control" id="evCap" name="capacity"
                                           min="1" placeholder="Unlimited if blank">
                                </div>

                                <div class="col-12">
                                    <label for="evDesc" class="form-label fw-medium">Description</label>
                                    <textarea class="form-control" id="evDesc" name="description" rows="3"
                                              required placeholder="Objectives, eligibility, and highlights"></textarea>
                                    <div class="invalid-feedback">Event description is required.</div>
                                </div>

                                <div class="col-md-4">
                                    <label for="evDate" class="form-label fw-medium">Event Date</label>
                                    <input type="date" class="form-control" id="evDate" name="eventDate" required>
                                    <div class="invalid-feedback">Event date is required.</div>
                                </div>

                                <div class="col-md-4">
                                    <label for="evStart" class="form-label fw-medium">Start Time</label>
                                    <input type="time" class="form-control" id="evStart" name="startTime">
                                </div>

                                <div class="col-md-4">
                                    <label for="evEnd" class="form-label fw-medium">End Time</label>
                                    <input type="time" class="form-control" id="evEnd" name="endTime">
                                </div>

                                <div class="col-md-6">
                                    <label for="evVenue" class="form-label fw-medium">Venue</label>
                                    <input type="text" class="form-control" id="evVenue" name="venue"
                                           maxlength="180" required placeholder="e.g., Seminar Hall B">
                                    <div class="invalid-feedback">Venue is required.</div>
                                </div>

                                <div class="col-md-6">
                                    <label for="evOrg" class="form-label fw-medium">Organizer</label>
                                    <input type="text" class="form-control" id="evOrg" name="organizer"
                                           maxlength="150" placeholder="Club / Department Organizer">
                                </div>

                                <div class="col-md-6">
                                    <label for="evDeadline" class="form-label fw-medium">Registration Deadline (Optional)</label>
                                    <input type="datetime-local" class="form-control" id="evDeadline" name="registrationDeadline">
                                </div>

                                <div class="col-12">
                                    <span class="form-label fw-medium d-block">Associated Club(s)</span>
                                    <div class="d-flex flex-wrap gap-3">
                                        <% for (int i = 0; i < creatableClubs.size(); i++) {
                                            Club c = creatableClubs.get(i);
                                        %>
                                            <div class="form-check">
                                                <input class="form-check-input" type="checkbox" name="clubIds"
                                                       value="<%= c.getClubId() %>" id="createEvClub_<%= c.getClubId() %>"
                                                       <%= creatableClubs.size() == 1 ? "checked" : "" %>>
                                                <label class="form-check-label" for="createEvClub_<%= c.getClubId() %>">
                                                    <%= ViewHelper.esc(c.getClubName()) %>
                                                </label>
                                            </div>
                                        <% } %>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="modal-footer border-top-0">
                            <button type="button" class="btn btn-outline-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancel</button>
                            <button type="submit" class="btn btn-cf-primary rounded-pill px-4">
                                <%= isAdmin ? "Publish Event" : "Submit Proposal" %>
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    <% } %>
</main>

<%@ include file="/WEB-INF/fragments/footer.jspf" %>
