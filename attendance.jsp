<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.campusflow.model.Event" %>
<%@ page import="com.campusflow.model.EventRegistration" %>
<%@ page import="com.campusflow.util.ViewHelper" %>
<%
    Event event = (Event) request.getAttribute("event");
    request.setAttribute("pageTitle", "Attendance — " + (event != null ? event.getEventName() : "Event"));
    request.setAttribute("activeNav", "events");

    @SuppressWarnings("unchecked")
    List<EventRegistration> participants = (List<EventRegistration>) request.getAttribute("participants");
%>
<%@ include file="/WEB-INF/fragments/head.jspf" %>
<%@ include file="/WEB-INF/fragments/navbar.jspf" %>

<main class="container py-4">
    <%@ include file="/WEB-INF/fragments/flash.jspf" %>

    <% if (event != null) { %>
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
            <div>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb small mb-1">
                        <li class="breadcrumb-item"><a href="<%= ctx %>/events">Events</a></li>
                        <li class="breadcrumb-item">
                            <a href="<%= ctx %>/<%= event.isFlagship() ? "lakshya" : ("events?id=" + event.getEventId()) %>">
                                <%= ViewHelper.esc(event.getEventName()) %>
                            </a>
                        </li>
                        <li class="breadcrumb-item active" aria-current="page">Attendance</li>
                    </ol>
                </nav>
                <h1 class="h3 fw-bold mb-1">Participant Attendance Sheet</h1>
                <p class="text-body-secondary mb-0">
                    Event: <strong><%= ViewHelper.esc(event.getEventName()) %></strong>
                    &middot; <%= ViewHelper.esc(ViewHelper.formatDate(event.getEventDate(), "Date TBD")) %>
                </p>
            </div>
            <a href="<%= ctx %>/<%= event.isFlagship() ? "lakshya" : ("events?id=" + event.getEventId()) %>"
               class="btn btn-outline-secondary rounded-pill px-4">
                <i class="bi bi-arrow-left me-1"></i>Back to Event
            </a>
        </div>

        <section class="cf-card p-4 cf-animate-in">
            <% if (participants != null && !participants.isEmpty()) { %>
                <form method="post" action="<%= ctx %>/attendance">
                    <input type="hidden" name="_csrf" value="<%= ViewHelper.csrf(request) %>">
                    <input type="hidden" name="eventId" value="<%= event.getEventId() %>">

                    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
                        <span class="small text-body-secondary">
                            Showing <strong><%= participants.size() %></strong> registered student(s). Saving updates existing entries without creating duplicates.
                        </span>
                        <button type="button" class="btn btn-sm btn-outline-success rounded-pill px-3"
                                onclick="document.querySelectorAll('.cf-att-present').forEach(function(r){r.checked=true;});">
                            <i class="bi bi-check2-all me-1"></i>Select All Present
                        </button>
                    </div>

                    <div class="table-responsive">
                        <table class="table align-middle">
                            <thead>
                                <tr>
                                    <th>Student Name</th>
                                    <th>Email</th>
                                    <th>Current Record</th>
                                    <th>Mark Attendance</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% for (EventRegistration reg : participants) {
                                    String curr = reg.getAttendanceStatus();
                                %>
                                    <tr>
                                        <td class="fw-semibold"><%= ViewHelper.esc(reg.getStudentName()) %></td>
                                        <td class="small text-body-secondary"><%= ViewHelper.esc(reg.getStudentEmail()) %></td>
                                        <td>
                                            <% if ("PRESENT".equals(curr)) { %>
                                                <span class="badge rounded-pill badge-soft-success"><i class="bi bi-check-circle-fill me-1"></i>Present</span>
                                            <% } else if ("ABSENT".equals(curr)) { %>
                                                <span class="badge rounded-pill badge-soft-danger"><i class="bi bi-x-circle-fill me-1"></i>Absent</span>
                                            <% } else { %>
                                                <span class="badge rounded-pill badge-soft-secondary">Unmarked</span>
                                            <% } %>
                                        </td>
                                        <td>
                                            <div class="btn-group btn-group-sm" role="group"
                                                 aria-label="Attendance for <%= ViewHelper.esc(reg.getStudentName()) %>">
                                                <input type="radio" class="btn-check cf-att-present"
                                                       name="status_<%= reg.getUserId() %>"
                                                       id="attP_<%= reg.getUserId() %>" value="PRESENT"
                                                       <%= "PRESENT".equals(curr) ? "checked" : "" %>>
                                                <label class="btn btn-outline-success" for="attP_<%= reg.getUserId() %>">
                                                    <i class="bi bi-check-lg me-1"></i>Present
                                                </label>

                                                <input type="radio" class="btn-check"
                                                       name="status_<%= reg.getUserId() %>"
                                                       id="attA_<%= reg.getUserId() %>" value="ABSENT"
                                                       <%= "ABSENT".equals(curr) ? "checked" : "" %>>
                                                <label class="btn btn-outline-danger" for="attA_<%= reg.getUserId() %>">
                                                    <i class="bi bi-x-lg me-1"></i>Absent
                                                </label>
                                            </div>
                                        </td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>

                    <div class="d-flex justify-content-end pt-3 border-top">
                        <button type="submit" class="btn btn-cf-primary rounded-pill px-4">
                            <i class="bi bi-save2 me-1"></i>Save Attendance Records
                        </button>
                    </div>
                </form>
            <% } else { %>
                <div class="text-center py-5 text-body-secondary">
                    <i class="bi bi-people fs-1 d-block mb-2"></i>
                    <h2 class="h5 fw-bold text-body">No Registered Participants Yet</h2>
                    <p class="mb-0">Attendance can be recorded as soon as students register for this event.</p>
                </div>
            <% } %>
        </section>
    <% } %>
</main>

<%@ include file="/WEB-INF/fragments/footer.jspf" %>
