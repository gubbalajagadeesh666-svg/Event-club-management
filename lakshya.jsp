<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.util.List" %>
<%@ page import="com.campusflow.model.Club" %>
<%@ page import="com.campusflow.model.Event" %>
<%@ page import="com.campusflow.model.EventRegistration" %>
<%@ page import="com.campusflow.model.User" %>
<%@ page import="com.campusflow.util.AuthUtil" %>
<%@ page import="com.campusflow.util.ViewHelper" %>
<%
    request.setAttribute("pageTitle", "Lakshya — Flagship College Event");
    request.setAttribute("activeNav", "lakshya");

    Event lakshya = (Event) request.getAttribute("lakshya");
    String role = AuthUtil.getUserRole(request);
    boolean isAdmin = User.ROLE_ADMIN.equals(role);
    boolean canStudentRegister = User.ROLE_STUDENT.equals(role) || User.ROLE_CLUB_LEADER.equals(role);

    Boolean canViewParticipantsObj = (Boolean) request.getAttribute("canViewParticipants");
    boolean canViewParticipants = canViewParticipantsObj != null && canViewParticipantsObj;

    @SuppressWarnings("unchecked")
    List<Club> allClubs = (List<Club>) request.getAttribute("allClubs");
    @SuppressWarnings("unchecked")
    List<EventRegistration> participants = (List<EventRegistration>) request.getAttribute("participants");

    LocalDate today = LocalDate.now();
    LocalDateTime now = LocalDateTime.now();
%>
<%@ include file="/WEB-INF/fragments/head.jspf" %>
<%@ include file="/WEB-INF/fragments/navbar.jspf" %>

<main class="container py-4">
    <%@ include file="/WEB-INF/fragments/flash.jspf" %>

    <% if (lakshya != null) {
        boolean regOpen = lakshya.isRegistrationOpen(today, now);
    %>
        <!-- Prominent Lakshya Banner -->
        <section class="lakshya-spotlight p-4 p-md-5 mb-4 cf-animate-in">
            <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
                <span class="badge rounded-pill bg-warning text-dark fw-bold px-3 py-2">
                    <i class="bi bi-trophy-fill me-1"></i>PRIMARY FLAGSHIP COLLEGE EVENT
                </span>
                <span class="badge rounded-pill bg-white bg-opacity-10 border border-light border-opacity-25 px-3 py-2">
                    <i class="bi <%= lakshya.getStatusIconClass() %> me-1 text-warning"></i>
                    <%= ViewHelper.esc(lakshya.getStatusDisplayName()) %>
                </span>
            </div>

            <div class="row g-4 align-items-center">
                <div class="col-lg-8">
                    <h1 class="display-4 fw-bold mb-3"><%= ViewHelper.esc(lakshya.getEventName()) %></h1>
                    <p class="lead text-white-50 mb-4"><%= ViewHelper.esc(lakshya.getDescription()) %></p>

                    <div class="mb-4">
                        <div class="small text-uppercase text-white-50 fw-semibold mb-2">Participating Clubs</div>
                        <div class="d-flex flex-wrap gap-2">
                            <% if (!lakshya.getAssociatedClubs().isEmpty()) {
                                for (Club ac : lakshya.getAssociatedClubs()) { %>
                                    <a href="<%= ctx %>/clubs?id=<%= ac.getClubId() %>"
                                       class="badge rounded-pill bg-white bg-opacity-10 border border-light border-opacity-25 px-3 py-2 text-white text-decoration-none">
                                        <i class="bi <%= ViewHelper.esc(ac.getIconClass()) %> text-warning me-1"></i>
                                        <%= ViewHelper.esc(ac.getClubName()) %>
                                    </a>
                            <%  }
                               } else { %>
                                <span class="text-white-50 small">Clubs to be linked by Administrator</span>
                            <% } %>
                        </div>
                    </div>

                    <div class="row g-3">
                        <div class="col-sm-6 col-md-3">
                            <div class="lakshya-glass-tile h-100">
                                <div class="small text-white-50"><i class="bi bi-calendar3 me-1"></i>Date</div>
                                <div class="fw-semibold mt-1">
                                    <%= ViewHelper.esc(ViewHelper.formatDate(lakshya.getEventDate(), "To be announced")) %>
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-6 col-md-3">
                            <div class="lakshya-glass-tile h-100">
                                <div class="small text-white-50"><i class="bi bi-clock me-1"></i>Schedule</div>
                                <div class="fw-semibold mt-1">
                                    <% if (lakshya.getStartTime() != null) { %>
                                        <%= ViewHelper.esc(ViewHelper.formatTime(lakshya.getStartTime(), "")) %>
                                        <%= lakshya.getEndTime() != null ? (" – " + ViewHelper.esc(ViewHelper.formatTime(lakshya.getEndTime(), ""))) : "" %>
                                    <% } else { %>
                                        To be announced
                                    <% } %>
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-6 col-md-3">
                            <div class="lakshya-glass-tile h-100">
                                <div class="small text-white-50"><i class="bi bi-geo-alt-fill me-1"></i>Venue</div>
                                <div class="fw-semibold mt-1">
                                    <%= lakshya.getVenue() != null ? ViewHelper.esc(lakshya.getVenue()) : "To be announced" %>
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-6 col-md-3">
                            <div class="lakshya-glass-tile h-100">
                                <div class="small text-white-50"><i class="bi bi-building me-1"></i>Organizer</div>
                                <div class="fw-semibold mt-1">
                                    <%= lakshya.getOrganizer() != null ? ViewHelper.esc(lakshya.getOrganizer()) : "To be announced" %>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Registration & Action Panel -->
                <div class="col-lg-4">
                    <div class="lakshya-glass-tile p-4 text-center">
                        <div class="display-5 fw-bold text-warning mb-1"><%= lakshya.getRegisteredCount() %></div>
                        <div class="small text-white-50 mb-3">
                            Registered Participants
                            <% if (lakshya.getCapacity() != null) { %>
                                (Capacity: <%= lakshya.getCapacity() %>, Remaining: <%= lakshya.getRemainingSeats() %>)
                            <% } %>
                        </div>

                        <% if (lakshya.getRegistrationDeadline() != null) { %>
                            <div class="small text-white-50 mb-3">
                                <i class="bi bi-hourglass-bottom me-1"></i>Deadline:
                                <strong class="text-white"><%= ViewHelper.esc(ViewHelper.formatDateTime(lakshya.getRegistrationDeadline(), "")) %></strong>
                            </div>
                        <% } %>

                        <% if (canStudentRegister) { %>
                            <% if (lakshya.isUserRegistered()) { %>
                                <div class="alert alert-success py-2 small mb-2">
                                    <i class="bi bi-check-circle-fill me-1"></i>You are registered for Lakshya!
                                </div>
                                <form method="post" action="<%= ctx %>/registrations"
                                      data-confirm="Cancel your registration for Lakshya?">
                                    <input type="hidden" name="_csrf" value="<%= ViewHelper.csrf(request) %>">
                                    <input type="hidden" name="action" value="cancel">
                                    <input type="hidden" name="eventId" value="<%= lakshya.getEventId() %>">
                                    <input type="hidden" name="returnTo" value="lakshya">
                                    <button type="submit" class="btn btn-sm btn-outline-light rounded-pill w-100">
                                        Cancel Registration
                                    </button>
                                </form>
                            <% } else if (regOpen) { %>
                                <form method="post" action="<%= ctx %>/registrations">
                                    <input type="hidden" name="_csrf" value="<%= ViewHelper.csrf(request) %>">
                                    <input type="hidden" name="action" value="register">
                                    <input type="hidden" name="eventId" value="<%= lakshya.getEventId() %>">
                                    <input type="hidden" name="returnTo" value="lakshya">
                                    <button type="submit" class="btn btn-warning btn-lg rounded-pill fw-bold text-dark w-100">
                                        <i class="bi bi-check2-circle me-1"></i>Register for Lakshya
                                    </button>
                                </form>
                            <% } else { %>
                                <div class="p-2 rounded-3 bg-white bg-opacity-10 small text-white-50">
                                    <% if (Event.STATUS_SCHEDULE_PENDING.equals(lakshya.getStatus()) || lakshya.getEventDate() == null) { %>
                                        <i class="bi bi-info-circle me-1"></i>Registration opens once official schedule &amp; venue are published by Administration.
                                    <% } else if (lakshya.isFull()) { %>
                                        <i class="bi bi-exclamation-circle me-1"></i>Event capacity is full.
                                    <% } else { %>
                                        <i class="bi bi-lock me-1"></i>Registration is currently closed.
                                    <% } %>
                                </div>
                            <% } %>
                        <% } %>

                        <% if (isAdmin) { %>
                            <hr class="border-light border-opacity-25 my-3">
                            <button type="button" class="btn btn-outline-warning rounded-pill w-100"
                                    data-bs-toggle="modal" data-bs-target="#manageLakshyaModal">
                                <i class="bi bi-sliders me-1"></i>Configure Official Details
                            </button>
                        <% } %>

                        <% if (canViewParticipants) { %>
                            <a href="<%= ctx %>/attendance?eventId=<%= lakshya.getEventId() %>"
                               class="btn btn-sm btn-outline-light rounded-pill w-100 mt-2">
                                <i class="bi bi-clipboard2-check me-1"></i>Manage Attendance
                            </a>
                        <% } %>
                    </div>
                </div>
            </div>
        </section>

        <!-- Participant Roster for Authorized Organizers -->
        <% if (canViewParticipants) { %>
            <section class="cf-card p-4 cf-animate-in">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h2 class="h5 fw-bold mb-0">
                        <i class="bi bi-people-fill text-primary me-2"></i>Lakshya Registered Participants (<%= participants != null ? participants.size() : 0 %>)
                    </h2>
                    <a href="<%= ctx %>/attendance?eventId=<%= lakshya.getEventId() %>"
                       class="btn btn-sm btn-outline-primary rounded-pill px-3">
                        Mark Attendance
                    </a>
                </div>
                <% if (participants != null && !participants.isEmpty()) { %>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead>
                                <tr>
                                    <th>Student Name</th>
                                    <th>Email</th>
                                    <th>Registered At</th>
                                    <th>Attendance</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% for (EventRegistration p : participants) { %>
                                    <tr>
                                        <td class="fw-medium"><%= ViewHelper.esc(p.getStudentName()) %></td>
                                        <td><%= ViewHelper.esc(p.getStudentEmail()) %></td>
                                        <td class="small text-body-secondary"><%= ViewHelper.esc(ViewHelper.formatDateTime(p.getRegistrationDate(), "-")) %></td>
                                        <td>
                                            <% if ("PRESENT".equals(p.getAttendanceStatus())) { %>
                                                <span class="badge rounded-pill badge-soft-success"><i class="bi bi-check-circle-fill me-1"></i>Present</span>
                                            <% } else if ("ABSENT".equals(p.getAttendanceStatus())) { %>
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
                    <p class="text-body-secondary mb-0">No students have registered for Lakshya yet.</p>
                <% } %>
            </section>
        <% } %>

        <!-- Admin Modal to Complete/Update Official Lakshya Details -->
        <% if (isAdmin) { %>
            <div class="modal fade" id="manageLakshyaModal" tabindex="-1" aria-labelledby="manageLakshyaModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-lg modal-dialog-centered">
                    <div class="modal-content rounded-4 border-0 shadow">
                        <form method="post" action="<%= ctx %>/lakshya" class="needs-validation" novalidate>
                            <input type="hidden" name="_csrf" value="<%= ViewHelper.csrf(request) %>">

                            <div class="modal-header border-bottom-0 pb-0">
                                <h2 class="modal-title h5 fw-bold" id="manageLakshyaModalLabel">
                                    <i class="bi bi-stars text-warning me-2"></i>Update Official Lakshya Record
                                </h2>
                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>

                            <div class="modal-body">
                                <div class="row g-3">
                                    <div class="col-12">
                                        <label for="lakDesc" class="form-label fw-medium">Official Description</label>
                                        <textarea class="form-control" id="lakDesc" name="description" rows="3" required><%= ViewHelper.esc(lakshya.getDescription()) %></textarea>
                                    </div>

                                    <div class="col-md-4">
                                        <label for="lakDate" class="form-label fw-medium">Official Event Date</label>
                                        <input type="date" class="form-control" id="lakDate" name="eventDate"
                                               value="<%= ViewHelper.toInputDate(lakshya.getEventDate()) %>">
                                    </div>
                                    <div class="col-md-4">
                                        <label for="lakStart" class="form-label fw-medium">Start Time</label>
                                        <input type="time" class="form-control" id="lakStart" name="startTime"
                                               value="<%= ViewHelper.toInputTime(lakshya.getStartTime()) %>">
                                    </div>
                                    <div class="col-md-4">
                                        <label for="lakEnd" class="form-label fw-medium">End Time</label>
                                        <input type="time" class="form-control" id="lakEnd" name="endTime"
                                               value="<%= ViewHelper.toInputTime(lakshya.getEndTime()) %>">
                                    </div>

                                    <div class="col-md-6">
                                        <label for="lakVenue" class="form-label fw-medium">Venue</label>
                                        <input type="text" class="form-control" id="lakVenue" name="venue" maxlength="180"
                                               value="<%= ViewHelper.esc(lakshya.getVenue()) %>"
                                               placeholder="Enter official venue when finalized">
                                    </div>
                                    <div class="col-md-6">
                                        <label for="lakOrg" class="form-label fw-medium">Organizer / Convenor</label>
                                        <input type="text" class="form-control" id="lakOrg" name="organizer" maxlength="150"
                                               value="<%= ViewHelper.esc(lakshya.getOrganizer()) %>"
                                               placeholder="e.g., Student Cultural &amp; Academic Council">
                                    </div>

                                    <div class="col-md-5">
                                        <label for="lakDeadline" class="form-label fw-medium">Registration Deadline</label>
                                        <input type="datetime-local" class="form-control" id="lakDeadline" name="registrationDeadline"
                                               value="<%= ViewHelper.toInputDateTime(lakshya.getRegistrationDeadline()) %>">
                                    </div>
                                    <div class="col-md-3">
                                        <label for="lakCap" class="form-label fw-medium">Capacity (Blank = Unlimited)</label>
                                        <input type="number" class="form-control" id="lakCap" name="capacity" min="1"
                                               value="<%= lakshya.getCapacity() != null ? lakshya.getCapacity() : "" %>">
                                    </div>
                                    <div class="col-md-4">
                                        <label for="lakStatus" class="form-label fw-medium">Publication Status</label>
                                        <select class="form-select" id="lakStatus" name="status">
                                            <option value="SCHEDULE_PENDING" <%= Event.STATUS_SCHEDULE_PENDING.equals(lakshya.getStatus()) ? "selected" : "" %>>Schedule Pending</option>
                                            <option value="APPROVED" <%= Event.STATUS_APPROVED.equals(lakshya.getStatus()) ? "selected" : "" %>>Approved / Open Registration</option>
                                            <option value="COMPLETED" <%= Event.STATUS_COMPLETED.equals(lakshya.getStatus()) ? "selected" : "" %>>Completed</option>
                                            <option value="CANCELLED" <%= Event.STATUS_CANCELLED.equals(lakshya.getStatus()) ? "selected" : "" %>>Cancelled</option>
                                        </select>
                                    </div>

                                    <div class="col-12">
                                        <span class="form-label fw-medium d-block">Associated Clubs</span>
                                        <div class="d-flex flex-wrap gap-3">
                                            <% if (allClubs != null) {
                                                for (Club c : allClubs) { %>
                                                <div class="form-check">
                                                    <input class="form-check-input" type="checkbox" name="clubIds"
                                                           value="<%= c.getClubId() %>" id="lakClub_<%= c.getClubId() %>"
                                                           <%= lakshya.hasClub(c.getClubId()) ? "checked" : "" %>>
                                                    <label class="form-check-label" for="lakClub_<%= c.getClubId() %>">
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
                                <button type="submit" class="btn btn-cf-primary rounded-pill px-4">Save Official Lakshya Details</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        <% } %>
    <% } else { %>
        <div class="cf-card p-5 text-center">
            <h1 class="h4 fw-bold">Lakshya Not Found</h1>
            <p class="text-body-secondary mb-0">Ensure <code>database/schema.sql</code> has been executed on <code>campusflow_db</code>.</p>
        </div>
    <% } %>
</main>

<%@ include file="/WEB-INF/fragments/footer.jspf" %>
