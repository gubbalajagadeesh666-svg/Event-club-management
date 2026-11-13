<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.util.List" %>
<%@ page import="com.campusflow.model.Club" %>
<%@ page import="com.campusflow.model.Event" %>
<%@ page import="com.campusflow.model.EventApproval" %>
<%@ page import="com.campusflow.model.EventRegistration" %>
<%@ page import="com.campusflow.model.User" %>
<%@ page import="com.campusflow.util.AuthUtil" %>
<%@ page import="com.campusflow.util.ViewHelper" %>
<%
    Event event = (Event) request.getAttribute("event");
    request.setAttribute("pageTitle", event != null ? event.getEventName() : "Event Details");
    request.setAttribute("activeNav", "events");

    String role = AuthUtil.getUserRole(request);
    boolean isAdmin = User.ROLE_ADMIN.equals(role);
    boolean canStudentRegister = User.ROLE_STUDENT.equals(role) || User.ROLE_CLUB_LEADER.equals(role);

    Boolean isOrganizerObj = (Boolean) request.getAttribute("isOrganizer");
    boolean isOrganizer = isOrganizerObj != null && isOrganizerObj;
    Boolean canEditObj = (Boolean) request.getAttribute("canEditEvent");
    boolean canEditEvent = canEditObj != null && canEditObj;
    Boolean canReviewObj = (Boolean) request.getAttribute("canReviewProposal");
    boolean canReviewProposal = canReviewObj != null && canReviewObj;

    @SuppressWarnings("unchecked")
    List<EventApproval> approvalHistory = (List<EventApproval>) request.getAttribute("approvalHistory");
    @SuppressWarnings("unchecked")
    List<EventRegistration> participants = (List<EventRegistration>) request.getAttribute("participants");
    @SuppressWarnings("unchecked")
    List<Club> editableClubs = (List<Club>) request.getAttribute("editableClubs");

    LocalDate today = LocalDate.now();
    LocalDateTime now = LocalDateTime.now();
%>
<%@ include file="/WEB-INF/fragments/head.jspf" %>
<%@ include file="/WEB-INF/fragments/navbar.jspf" %>

<main class="container py-4">
    <%@ include file="/WEB-INF/fragments/flash.jspf" %>

    <% if (event != null) {
        boolean regOpen = event.isRegistrationOpen(today, now);
    %>
        <nav aria-label="breadcrumb" class="mb-3">
            <ol class="breadcrumb small mb-0">
                <li class="breadcrumb-item"><a href="<%= ctx %>/dashboard">Dashboard</a></li>
                <li class="breadcrumb-item"><a href="<%= ctx %>/events">Events</a></li>
                <li class="breadcrumb-item active" aria-current="page"><%= ViewHelper.esc(event.getEventName()) %></li>
            </ol>
        </nav>

        <div class="row g-4">
            <!-- Left Column: Event Details & Approval History -->
            <div class="col-lg-8">
                <section class="cf-card p-4 p-md-5 mb-4 cf-animate-in">
                    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
                        <span class="badge rounded-pill <%= event.getStatusBadgeClass() %> px-3 py-2">
                            <i class="bi <%= event.getStatusIconClass() %> me-1"></i>
                            <%= ViewHelper.esc(event.getStatusDisplayName()) %>
                        </span>

                        <div class="d-flex flex-wrap gap-2">
                            <% if (canEditEvent && !event.isFlagship()) { %>
                                <button type="button" class="btn btn-sm btn-outline-primary rounded-pill px-3"
                                        data-bs-toggle="modal" data-bs-target="#editEventModal">
                                    <i class="bi bi-pencil-square me-1"></i>Edit Event
                                </button>
                            <% } %>
                            <% if (event.isFlagship()) { %>
                                <a href="<%= ctx %>/lakshya" class="btn btn-sm btn-warning rounded-pill px-3 fw-semibold text-dark">
                                    <i class="bi bi-stars me-1"></i>Open Dedicated Lakshya Page
                                </a>
                            <% } %>
                        </div>
                    </div>

                    <h1 class="h2 fw-bold mb-3"><%= ViewHelper.esc(event.getEventName()) %></h1>
                    <p class="text-body-secondary mb-4"><%= ViewHelper.esc(event.getDescription()) %></p>

                    <% if (!event.getAssociatedClubs().isEmpty()) { %>
                        <div class="mb-4">
                            <div class="small fw-semibold text-uppercase text-body-secondary mb-2">Associated Club(s)</div>
                            <div class="d-flex flex-wrap gap-2">
                                <% for (Club ac : event.getAssociatedClubs()) { %>
                                    <a href="<%= ctx %>/clubs?id=<%= ac.getClubId() %>"
                                       class="btn btn-sm btn-outline-secondary rounded-pill">
                                        <i class="bi <%= ViewHelper.esc(ac.getIconClass()) %> me-1 text-primary"></i>
                                        <%= ViewHelper.esc(ac.getClubName()) %>
                                    </a>
                                <% } %>
                            </div>
                        </div>
                    <% } %>

                    <div class="row g-3 pt-3 border-top">
                        <div class="col-sm-6">
                            <div class="p-3 rounded-3 bg-body-tertiary h-100">
                                <div class="small text-body-secondary">Date &amp; Time</div>
                                <div class="fw-semibold mt-1">
                                    <i class="bi bi-calendar3 text-primary me-1"></i>
                                    <%= ViewHelper.esc(ViewHelper.formatDate(event.getEventDate(), "To be announced")) %>
                                </div>
                                <div class="small text-body-secondary mt-1">
                                    <% if (event.getStartTime() != null) { %>
                                        <%= ViewHelper.esc(ViewHelper.formatTime(event.getStartTime(), "")) %>
                                        <%= event.getEndTime() != null ? (" – " + ViewHelper.esc(ViewHelper.formatTime(event.getEndTime(), ""))) : "" %>
                                    <% } else { %>
                                        Schedule time not specified
                                    <% } %>
                                </div>
                            </div>
                        </div>

                        <div class="col-sm-6">
                            <div class="p-3 rounded-3 bg-body-tertiary h-100">
                                <div class="small text-body-secondary">Venue &amp; Organizer</div>
                                <div class="fw-semibold mt-1">
                                    <i class="bi bi-geo-alt-fill text-primary me-1"></i>
                                    <%= event.getVenue() != null ? ViewHelper.esc(event.getVenue()) : "To be announced" %>
                                </div>
                                <div class="small text-body-secondary mt-1">
                                    Organizer: <%= event.getOrganizer() != null ? ViewHelper.esc(event.getOrganizer()) : "Campus Club Committee" %>
                                </div>
                            </div>
                        </div>

                        <div class="col-sm-6">
                            <div class="p-3 rounded-3 bg-body-tertiary h-100">
                                <div class="small text-body-secondary">Registration Deadline</div>
                                <div class="fw-semibold mt-1">
                                    <%= ViewHelper.esc(ViewHelper.formatDateTime(event.getRegistrationDeadline(), "Open until event date")) %>
                                </div>
                            </div>
                        </div>

                        <div class="col-sm-6">
                            <div class="p-3 rounded-3 bg-body-tertiary h-100">
                                <div class="small text-body-secondary">Proposal / Record Metadata</div>
                                <div class="fw-semibold mt-1">
                                    Created by: <%= event.getCreatedByName() != null ? ViewHelper.esc(event.getCreatedByName()) : "System Seed" %>
                                </div>
                                <div class="small text-body-secondary mt-1">
                                    Created: <%= ViewHelper.esc(ViewHelper.formatDateTime(event.getCreatedAt(), "-")) %>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- Proposal Review Box (Authorized Faculty Coordinator or Admin) -->
                <% if (canReviewProposal) { %>
                    <section class="cf-card p-4 mb-4 border-warning border-opacity-50 cf-animate-in">
                        <h2 class="h5 fw-bold mb-2">
                            <i class="bi bi-clipboard2-check-fill text-warning me-2"></i>Review Event Proposal
                        </h2>
                        <p class="small text-body-secondary mb-3">
                            Approve to open this event for student registration, or reject with feedback for the Club Leader.
                        </p>
                        <form method="post" action="<%= ctx %>/events">
                            <input type="hidden" name="_csrf" value="<%= ViewHelper.csrf(request) %>">
                            <input type="hidden" name="action" value="review">
                            <input type="hidden" name="eventId" value="<%= event.getEventId() %>">

                            <div class="mb-3">
                                <label for="reviewRemarks" class="form-label small fw-medium">Reviewer Remarks (Optional)</label>
                                <textarea class="form-control" id="reviewRemarks" name="remarks" rows="2" maxlength="500"
                                          placeholder="Optional notes regarding venue readiness, schedule, or suggestions"></textarea>
                            </div>

                            <div class="d-flex flex-wrap gap-2">
                                <button type="submit" name="decision" value="APPROVED" class="btn btn-success rounded-pill px-4">
                                    <i class="bi bi-check-circle-fill me-1"></i>Approve Proposal
                                </button>
                                <button type="submit" name="decision" value="REJECTED" class="btn btn-outline-danger rounded-pill px-4">
                                    <i class="bi bi-x-octagon-fill me-1"></i>Reject Proposal
                                </button>
                            </div>
                        </form>
                    </section>
                <% } %>

                <!-- Approval Audit History -->
                <% if (approvalHistory != null && !approvalHistory.isEmpty()) { %>
                    <section class="cf-card p-4 mb-4 cf-animate-in">
                        <h2 class="h6 text-uppercase fw-bold text-body-secondary mb-3">
                            <i class="bi bi-clock-history me-1"></i>Proposal Approval History
                        </h2>
                        <div class="d-grid gap-2">
                            <% for (EventApproval ap : approvalHistory) { %>
                                <div class="p-3 rounded-3 border bg-body-tertiary small">
                                    <div class="d-flex flex-wrap justify-content-between gap-2 mb-1">
                                        <span>
                                            <strong><%= ViewHelper.esc(ap.getReviewerName()) %></strong>
                                            (<%= ViewHelper.esc(ViewHelper.roleDisplay(ap.getReviewerRole())) %>)
                                            marked proposal as
                                            <span class="badge <%= "APPROVED".equals(ap.getDecision()) ? "badge-soft-success" : "badge-soft-danger" %>">
                                                <%= ViewHelper.esc(ap.getDecision()) %>
                                            </span>
                                        </span>
                                        <span class="text-body-secondary"><%= ViewHelper.esc(ViewHelper.formatDateTime(ap.getReviewedAt(), "-")) %></span>
                                    </div>
                                    <% if (ap.getRemarks() != null && !ap.getRemarks().trim().isEmpty()) { %>
                                        <div class="text-body-secondary fst-italic">"<%= ViewHelper.esc(ap.getRemarks()) %>"</div>
                                    <% } %>
                                </div>
                            <% } %>
                        </div>
                    </section>
                <% } %>

                <!-- Registered Participants Table for Authorized Organizers -->
                <% if (isOrganizer) { %>
                    <section class="cf-card p-4 cf-animate-in">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h2 class="h5 fw-bold mb-0">
                                <i class="bi bi-people-fill text-primary me-2"></i>Registered Participants (<%= participants != null ? participants.size() : 0 %>)
                            </h2>
                            <a href="<%= ctx %>/attendance?eventId=<%= event.getEventId() %>"
                               class="btn btn-sm btn-cf-primary rounded-pill px-3">
                                <i class="bi bi-clipboard2-check me-1"></i>Mark Attendance
                            </a>
                        </div>

                        <% if (participants != null && !participants.isEmpty()) { %>
                            <div class="table-responsive">
                                <table class="table table-hover align-middle mb-0">
                                    <thead>
                                        <tr>
                                            <th>Student Name</th>
                                            <th>Email</th>
                                            <th>Registered On</th>
                                            <th>Attendance</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (EventRegistration reg : participants) { %>
                                            <tr>
                                                <td class="fw-medium"><%= ViewHelper.esc(reg.getStudentName()) %></td>
                                                <td><%= ViewHelper.esc(reg.getStudentEmail()) %></td>
                                                <td class="small text-body-secondary"><%= ViewHelper.esc(ViewHelper.formatDateTime(reg.getRegistrationDate(), "-")) %></td>
                                                <td>
                                                    <% if ("PRESENT".equals(reg.getAttendanceStatus())) { %>
                                                        <span class="badge rounded-pill badge-soft-success"><i class="bi bi-check-circle-fill me-1"></i>Present</span>
                                                    <% } else if ("ABSENT".equals(reg.getAttendanceStatus())) { %>
                                                        <span class="badge rounded-pill badge-soft-danger"><i class="bi bi-x-circle-fill me-1"></i>Absent</span>
                                                    <% } else { %>
                                                        <span class="badge rounded-pill badge-soft-secondary">Not Marked</span>
                                                    <% } %>
                                                </td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        <% } else { %>
                            <p class="text-body-secondary mb-0">No students have registered for this event yet.</p>
                        <% } %>
                    </section>
                <% } %>
            </div>

            <!-- Right Column: Registration Card & Administrative Status Controls -->
            <div class="col-lg-4">
                <section class="cf-card p-4 mb-4 cf-animate-in">
                    <h2 class="h5 fw-bold mb-3">Registration Status</h2>

                    <div class="p-3 rounded-3 bg-body-tertiary text-center mb-3">
                        <div class="display-6 fw-bold text-primary"><%= event.getRegisteredCount() %></div>
                        <div class="small text-body-secondary">
                            <% if (event.getCapacity() != null) { %>
                                of <%= event.getCapacity() %> seats filled (<%= event.getRemainingSeats() %> available)
                            <% } else { %>
                                Registered Participants (Open Capacity)
                            <% } %>
                        </div>
                    </div>

                    <% if (canStudentRegister) { %>
                        <% if (event.isUserRegistered()) { %>
                            <div class="alert alert-success small d-flex align-items-center gap-2">
                                <i class="bi bi-check-circle-fill"></i>
                                <span>You are registered for this event.</span>
                            </div>
                            <form method="post" action="<%= ctx %>/registrations"
                                  data-confirm="Cancel your registration for '<%= ViewHelper.esc(event.getEventName()) %>'?">
                                <input type="hidden" name="_csrf" value="<%= ViewHelper.csrf(request) %>">
                                <input type="hidden" name="action" value="cancel">
                                <input type="hidden" name="eventId" value="<%= event.getEventId() %>">
                                <button type="submit" class="btn btn-outline-danger rounded-pill w-100">
                                    Cancel My Registration
                                </button>
                            </form>
                        <% } else if (regOpen) { %>
                            <form method="post" action="<%= ctx %>/registrations">
                                <input type="hidden" name="_csrf" value="<%= ViewHelper.csrf(request) %>">
                                <input type="hidden" name="action" value="register">
                                <input type="hidden" name="eventId" value="<%= event.getEventId() %>">
                                <button type="submit" class="btn btn-cf-primary btn-lg rounded-pill w-100">
                                    <i class="bi bi-check2-circle me-1"></i>Register Now
                                </button>
                            </form>
                        <% } else { %>
                            <div class="alert alert-secondary small mb-0">
                                <i class="bi bi-lock-fill me-1"></i>
                                <% if (event.isFull()) { %>
                                    Event is full.
                                <% } else if (event.isDeadlinePassed(now)) { %>
                                    Registration deadline has passed.
                                <% } else if (!Event.STATUS_APPROVED.equals(event.getStatus())) { %>
                                    Registration is unavailable while status is <strong><%= ViewHelper.esc(event.getStatusDisplayName()) %></strong>.
                                <% } else { %>
                                    Registration is closed.
                                <% } %>
                            </div>
                        <% } %>
                    <% } else { %>
                        <p class="small text-body-secondary mb-0">
                            Signed in as <strong><%= ViewHelper.esc(ViewHelper.roleDisplay(role)) %></strong>.
                            Student accounts can register here when registration is open.
                        </p>
                    <% } %>
                </section>

                <!-- Admin Status Lifecycle & Safe Delete Card -->
                <% if (isAdmin && !event.isFlagship()) { %>
                    <section class="cf-card p-4 cf-animate-in">
                        <h2 class="h6 text-uppercase fw-bold text-body-secondary mb-3">Administrative Lifecycle</h2>

                        <form method="post" action="<%= ctx %>/events" class="mb-3">
                            <input type="hidden" name="_csrf" value="<%= ViewHelper.csrf(request) %>">
                            <input type="hidden" name="action" value="changeStatus">
                            <input type="hidden" name="eventId" value="<%= event.getEventId() %>">
                            <label for="adminNewStatus" class="form-label small fw-medium">Set Event Status</label>
                            <div class="input-group input-group-sm">
                                <select class="form-select" id="adminNewStatus" name="newStatus">
                                    <option value="APPROVED" <%= Event.STATUS_APPROVED.equals(event.getStatus()) ? "selected" : "" %>>Approved / Open</option>
                                    <option value="COMPLETED" <%= Event.STATUS_COMPLETED.equals(event.getStatus()) ? "selected" : "" %>>Completed</option>
                                    <option value="CANCELLED" <%= Event.STATUS_CANCELLED.equals(event.getStatus()) ? "selected" : "" %>>Cancelled</option>
                                </select>
                                <button type="submit" class="btn btn-outline-primary">Update</button>
                            </div>
                        </form>

                        <form method="post" action="<%= ctx %>/events"
                              data-confirm="Permanently delete '<%= ViewHelper.esc(event.getEventName()) %>'? Events with student registrations cannot be deleted.">
                            <input type="hidden" name="_csrf" value="<%= ViewHelper.csrf(request) %>">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="eventId" value="<%= event.getEventId() %>">
                            <button type="submit" class="btn btn-sm btn-outline-danger rounded-pill w-100">
                                <i class="bi bi-trash3 me-1"></i>Delete Unused Event
                            </button>
                        </form>
                    </section>
                <% } %>
            </div>
        </div>

        <!-- Edit Event Modal -->
        <% if (canEditEvent && !event.isFlagship()) { %>
            <div class="modal fade" id="editEventModal" tabindex="-1" aria-labelledby="editEventModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-lg modal-dialog-centered">
                    <div class="modal-content rounded-4 border-0 shadow">
                        <form method="post" action="<%= ctx %>/events" class="needs-validation" novalidate>
                            <input type="hidden" name="_csrf" value="<%= ViewHelper.csrf(request) %>">
                            <input type="hidden" name="action" value="update">
                            <input type="hidden" name="eventId" value="<%= event.getEventId() %>">

                            <div class="modal-header border-bottom-0 pb-0">
                                <h2 class="modal-title h5 fw-bold" id="editEventModalLabel">
                                    <i class="bi bi-pencil-square text-primary me-2"></i>Edit Event Details
                                </h2>
                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>

                            <div class="modal-body">
                                <div class="row g-3">
                                    <div class="col-md-8">
                                        <label for="edEvName" class="form-label fw-medium">Event Name</label>
                                        <input type="text" class="form-control" id="edEvName" name="eventName"
                                               maxlength="150" required value="<%= ViewHelper.esc(event.getEventName()) %>">
                                    </div>
                                    <div class="col-md-4">
                                        <label for="edEvCap" class="form-label fw-medium">Capacity</label>
                                        <input type="number" class="form-control" id="edEvCap" name="capacity" min="1"
                                               value="<%= event.getCapacity() != null ? event.getCapacity() : "" %>">
                                    </div>
                                    <div class="col-12">
                                        <label for="edEvDesc" class="form-label fw-medium">Description</label>
                                        <textarea class="form-control" id="edEvDesc" name="description" rows="3" required><%= ViewHelper.esc(event.getDescription()) %></textarea>
                                    </div>
                                    <div class="col-md-4">
                                        <label for="edEvDate" class="form-label fw-medium">Event Date</label>
                                        <input type="date" class="form-control" id="edEvDate" name="eventDate" required
                                               value="<%= ViewHelper.toInputDate(event.getEventDate()) %>">
                                    </div>
                                    <div class="col-md-4">
                                        <label for="edEvStart" class="form-label fw-medium">Start Time</label>
                                        <input type="time" class="form-control" id="edEvStart" name="startTime"
                                               value="<%= ViewHelper.toInputTime(event.getStartTime()) %>">
                                    </div>
                                    <div class="col-md-4">
                                        <label for="edEvEnd" class="form-label fw-medium">End Time</label>
                                        <input type="time" class="form-control" id="edEvEnd" name="endTime"
                                               value="<%= ViewHelper.toInputTime(event.getEndTime()) %>">
                                    </div>
                                    <div class="col-md-6">
                                        <label for="edEvVenue" class="form-label fw-medium">Venue</label>
                                        <input type="text" class="form-control" id="edEvVenue" name="venue" maxlength="180"
                                               required value="<%= ViewHelper.esc(event.getVenue()) %>">
                                    </div>
                                    <div class="col-md-6">
                                        <label for="edEvOrg" class="form-label fw-medium">Organizer</label>
                                        <input type="text" class="form-control" id="edEvOrg" name="organizer" maxlength="150"
                                               value="<%= ViewHelper.esc(event.getOrganizer()) %>">
                                    </div>
                                    <div class="col-md-6">
                                        <label for="edEvDeadline" class="form-label fw-medium">Registration Deadline</label>
                                        <input type="datetime-local" class="form-control" id="edEvDeadline" name="registrationDeadline"
                                               value="<%= ViewHelper.toInputDateTime(event.getRegistrationDeadline()) %>">
                                    </div>
                                    <div class="col-12">
                                        <span class="form-label fw-medium d-block">Associated Club(s)</span>
                                        <div class="d-flex flex-wrap gap-3">
                                            <% if (editableClubs != null) {
                                                for (Club c : editableClubs) { %>
                                                <div class="form-check">
                                                    <input class="form-check-input" type="checkbox" name="clubIds"
                                                           value="<%= c.getClubId() %>" id="edClub_<%= c.getClubId() %>"
                                                           <%= event.hasClub(c.getClubId()) ? "checked" : "" %>>
                                                    <label class="form-check-label" for="edClub_<%= c.getClubId() %>">
                                                        <%= ViewHelper.esc(c.getClubName()) %>
                                                    </label>
                                                </div>
                                            <%  }
                                               } %>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="modal-footer border-top-0">
                                <button type="button" class="btn btn-outline-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancel</button>
                                <button type="submit" class="btn btn-cf-primary rounded-pill px-4">Save Updates</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        <% } %>
    <% } %>
</main>

<%@ include file="/WEB-INF/fragments/footer.jspf" %>
