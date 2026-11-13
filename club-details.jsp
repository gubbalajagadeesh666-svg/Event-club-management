<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
<%@ page import="com.campusflow.model.Club" %>
<%@ page import="com.campusflow.model.Event" %>
<%@ page import="com.campusflow.model.User" %>
<%@ page import="com.campusflow.util.AuthUtil" %>
<%@ page import="com.campusflow.util.ViewHelper" %>
<%
    Club club = (Club) request.getAttribute("club");
    request.setAttribute("pageTitle", club != null ? club.getClubName() : "Club Details");
    request.setAttribute("activeNav", "clubs");

    boolean isAdmin = AuthUtil.isAdmin(request);
    Boolean canManageObj = (Boolean) request.getAttribute("canManageClub");
    boolean canManageClub = canManageObj != null && canManageObj;
    Boolean canViewMembersObj = (Boolean) request.getAttribute("canViewMembers");
    boolean canViewMembers = canViewMembersObj != null && canViewMembersObj;

    @SuppressWarnings("unchecked")
    List<Event> clubEvents = (List<Event>) request.getAttribute("clubEvents");
    @SuppressWarnings("unchecked")
    List<User> clubMembers = (List<User>) request.getAttribute("clubMembers");
    @SuppressWarnings("unchecked")
    List<User> facultyOptions = (List<User>) request.getAttribute("facultyOptions");
    @SuppressWarnings("unchecked")
    List<User> leaderOptions = (List<User>) request.getAttribute("leaderOptions");

    boolean isCoreSeededClub = club != null
            && Arrays.asList("prakruthi", "spoorthi", "kruthi", "saheli").contains(club.getClubSlug());
%>
<%@ include file="/WEB-INF/fragments/head.jspf" %>
<%@ include file="/WEB-INF/fragments/navbar.jspf" %>

<main class="container py-4">
    <%@ include file="/WEB-INF/fragments/flash.jspf" %>

    <% if (club != null) { %>
        <nav aria-label="breadcrumb" class="mb-3">
            <ol class="breadcrumb small mb-0">
                <li class="breadcrumb-item"><a href="<%= ctx %>/dashboard">Dashboard</a></li>
                <li class="breadcrumb-item"><a href="<%= ctx %>/clubs">Clubs</a></li>
                <li class="breadcrumb-item active" aria-current="page"><%= ViewHelper.esc(club.getClubName()) %></li>
            </ol>
        </nav>

        <!-- Club Header Card -->
        <section class="cf-card mb-4 club-accent-<%= ViewHelper.esc(club.getAccentTheme()) %> cf-animate-in">
            <div class="club-banner-strip"></div>
            <div class="p-4 p-md-5">
                <div class="d-flex flex-column flex-lg-row justify-content-between align-items-lg-start gap-4">
                    <div class="d-flex align-items-start gap-3">
                        <div class="club-icon-box" style="width:64px;height:64px;font-size:1.85rem;">
                            <i class="bi <%= ViewHelper.esc(club.getIconClass()) %>"></i>
                        </div>
                        <div>
                            <div class="d-flex flex-wrap align-items-center gap-2 mb-2">
                                <h1 class="h2 fw-bold mb-0"><%= ViewHelper.esc(club.getClubName()) %></h1>
                                <span class="club-tag-pill">
                                    <i class="bi bi-people-fill"></i> <%= club.getMemberCount() %> Active Member(s)
                                </span>
                                <% if (club.isUserMember()) { %>
                                    <span class="badge rounded-pill badge-soft-success">
                                        <i class="bi bi-check-circle-fill me-1"></i>Member
                                    </span>
                                <% } %>
                            </div>
                            <p class="text-primary fw-medium mb-2"><%= ViewHelper.esc(club.getFocusArea()) %></p>
                            <p class="text-body-secondary mb-0"><%= ViewHelper.esc(club.getDescription()) %></p>
                        </div>
                    </div>

                    <div class="d-flex flex-wrap gap-2 flex-shrink-0">
                        <!-- Join / Leave Club Membership Action -->
                        <form method="post" action="<%= ctx %>/clubs" class="m-0">
                            <input type="hidden" name="_csrf" value="<%= ViewHelper.csrf(request) %>">
                            <input type="hidden" name="clubId" value="<%= club.getClubId() %>">
                            <% if (club.isUserMember()) { %>
                                <input type="hidden" name="action" value="leave">
                                <button type="submit" class="btn btn-outline-secondary rounded-pill px-4">
                                    <i class="bi bi-person-dash me-1"></i>Leave Club
                                </button>
                            <% } else { %>
                                <input type="hidden" name="action" value="join">
                                <button type="submit" class="btn btn-cf-primary rounded-pill px-4">
                                    <i class="bi bi-person-plus-fill me-1"></i>Join Club
                                </button>
                            <% } %>
                        </form>

                        <% if (canManageClub) { %>
                            <button type="button" class="btn btn-outline-primary rounded-pill px-3"
                                    data-bs-toggle="modal" data-bs-target="#editClubModal">
                                <i class="bi bi-pencil-square me-1"></i>Edit Club
                            </button>
                        <% } %>

                        <% if (isAdmin && !isCoreSeededClub) { %>
                            <form method="post" action="<%= ctx %>/clubs" class="m-0"
                                  data-confirm="Delete club '<%= ViewHelper.esc(club.getClubName()) %>'? Clubs with memberships or events cannot be deleted.">
                                <input type="hidden" name="_csrf" value="<%= ViewHelper.csrf(request) %>">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="clubId" value="<%= club.getClubId() %>">
                                <button type="submit" class="btn btn-outline-danger rounded-pill px-3">
                                    <i class="bi bi-trash3 me-1"></i>Delete
                                </button>
                            </form>
                        <% } %>
                    </div>
                </div>

                <div class="row g-3 mt-4 pt-4 border-top">
                    <div class="col-md-6">
                        <div class="p-3 rounded-3 bg-body-tertiary h-100">
                            <h2 class="h6 fw-bold mb-2"><i class="bi bi-stars text-primary me-2"></i>Signature Activities</h2>
                            <p class="small text-body-secondary mb-0"><%= ViewHelper.esc(club.getActivities()) %></p>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="p-3 rounded-3 bg-body-tertiary h-100 small">
                            <h2 class="h6 fw-bold mb-2"><i class="bi bi-person-badge text-primary me-2"></i>Club Leadership</h2>
                            <div class="d-flex justify-content-between py-1 border-bottom">
                                <span class="text-body-secondary">Faculty Coordinator</span>
                                <span class="fw-semibold">
                                    <%= club.getCoordinatorName() != null ? ViewHelper.esc(club.getCoordinatorName()) : "Not yet assigned" %>
                                </span>
                            </div>
                            <div class="d-flex justify-content-between pt-2">
                                <span class="text-body-secondary">Student Club Leader</span>
                                <span class="fw-semibold">
                                    <%= club.getLeaderName() != null ? ViewHelper.esc(club.getLeaderName()) : "Not yet assigned" %>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Associated Events Section -->
        <section class="cf-card p-4 mb-4 cf-animate-in">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h2 class="h5 fw-bold mb-0">
                    <i class="bi bi-calendar2-event text-primary me-2"></i>Events Associated with <%= ViewHelper.esc(club.getClubName()) %>
                </h2>
                <% if (canManageClub) { %>
                    <a href="<%= ctx %>/events" class="btn btn-sm btn-outline-primary rounded-pill px-3">
                        <i class="bi bi-plus-circle me-1"></i><%= isAdmin ? "Create Event" : "Propose Event" %>
                    </a>
                <% } %>
            </div>

            <% if (clubEvents != null && !clubEvents.isEmpty()) { %>
                <div class="row g-3">
                    <% for (Event ev : clubEvents) { %>
                        <div class="col-md-6">
                            <div class="p-3 rounded-3 border h-100 d-flex flex-column justify-content-between">
                                <div>
                                    <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-2">
                                        <h3 class="h6 fw-bold mb-0"><%= ViewHelper.esc(ev.getEventName()) %></h3>
                                        <span class="badge rounded-pill <%= ev.getStatusBadgeClass() %>">
                                            <i class="bi <%= ev.getStatusIconClass() %> me-1"></i><%= ViewHelper.esc(ev.getStatusDisplayName()) %>
                                        </span>
                                    </div>
                                    <p class="small text-body-secondary mb-2"><%= ViewHelper.esc(ev.getDescription()) %></p>
                                </div>
                                <div class="pt-2 border-top d-flex justify-content-between align-items-center small">
                                    <span class="text-body-secondary">
                                        <i class="bi bi-calendar3 me-1"></i><%= ViewHelper.esc(ViewHelper.formatDate(ev.getEventDate(), "Date TBD")) %>
                                    </span>
                                    <a href="<%= ctx %>/<%= ev.isFlagship() ? "lakshya" : ("events?id=" + ev.getEventId()) %>"
                                       class="fw-semibold text-decoration-none">
                                        Open Event &rarr;
                                    </a>
                                </div>
                            </div>
                        </div>
                    <% } %>
                </div>
            <% } else { %>
                <p class="text-body-secondary mb-0">No events are currently linked to this club.</p>
            <% } %>
        </section>

        <!-- Active Members Roster (Visible to Admin, Assigned Coordinator, or Assigned Club Leader) -->
        <% if (canViewMembers) { %>
            <section class="cf-card p-4 cf-animate-in">
                <h2 class="h5 fw-bold mb-3">
                    <i class="bi bi-people text-primary me-2"></i>Active Club Members (<%= clubMembers != null ? clubMembers.size() : 0 %>)
                </h2>
                <% if (clubMembers != null && !clubMembers.isEmpty()) { %>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead>
                                <tr>
                                    <th>Member Name</th>
                                    <th>Email</th>
                                    <th>Campus Role</th>
                                    <th>Joined On</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% for (User m : clubMembers) { %>
                                    <tr>
                                        <td class="fw-medium"><%= ViewHelper.esc(m.getFullName()) %></td>
                                        <td><%= ViewHelper.esc(m.getEmail()) %></td>
                                        <td><span class="badge text-bg-secondary"><%= ViewHelper.esc(m.getRoleDisplayName()) %></span></td>
                                        <td class="small text-body-secondary"><%= ViewHelper.esc(ViewHelper.formatDateTime(m.getCreatedAt(), "-")) %></td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                <% } else { %>
                    <p class="text-body-secondary mb-0">No members have joined this club yet.</p>
                <% } %>
            </section>
        <% } %>

        <!-- Edit Club Modal (Admin has full control; Assigned Club Leader edits permitted fields) -->
        <% if (canManageClub) { %>
            <div class="modal fade" id="editClubModal" tabindex="-1" aria-labelledby="editClubModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-lg modal-dialog-centered">
                    <div class="modal-content rounded-4 border-0 shadow">
                        <form method="post" action="<%= ctx %>/clubs" class="needs-validation" novalidate>
                            <input type="hidden" name="_csrf" value="<%= ViewHelper.csrf(request) %>">
                            <input type="hidden" name="action" value="update">
                            <input type="hidden" name="clubId" value="<%= club.getClubId() %>">

                            <div class="modal-header border-bottom-0 pb-0">
                                <h2 class="modal-title h5 fw-bold" id="editClubModalLabel">
                                    <i class="bi bi-pencil-square text-primary me-2"></i>Edit <%= ViewHelper.esc(club.getClubName()) %>
                                </h2>
                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>

                            <div class="modal-body">
                                <div class="row g-3">
                                    <% if (isAdmin) { %>
                                        <div class="col-md-6">
                                            <label for="editClubName" class="form-label fw-medium">Club Name</label>
                                            <input type="text" class="form-control" id="editClubName" name="clubName"
                                                   maxlength="100" required value="<%= ViewHelper.esc(club.getClubName()) %>">
                                        </div>
                                        <div class="col-md-3">
                                            <label for="editClubIcon" class="form-label fw-medium">Icon</label>
                                            <select class="form-select" id="editClubIcon" name="iconClass">
                                                <% for (String ic : Arrays.asList("bi-tree-fill","bi-journal-richtext","bi-music-note-beamed",
                                                        "bi-heart-pulse-fill","bi-stars","bi-cpu-fill","bi-lightbulb-fill",
                                                        "bi-camera-reels-fill","bi-trophy-fill","bi-globe-central-south-asia")) { %>
                                                    <option value="<%= ic %>" <%= ic.equals(club.getIconClass()) ? "selected" : "" %>><%= ic %></option>
                                                <% } %>
                                            </select>
                                        </div>
                                        <div class="col-md-3">
                                            <label for="editClubTheme" class="form-label fw-medium">Accent Theme</label>
                                            <select class="form-select" id="editClubTheme" name="accentTheme">
                                                <% for (String th : Arrays.asList("emerald","sky","amber","rose","indigo")) { %>
                                                    <option value="<%= th %>" <%= th.equals(club.getAccentTheme()) ? "selected" : "" %>><%= th %></option>
                                                <% } %>
                                            </select>
                                        </div>
                                    <% } %>

                                    <div class="col-12">
                                        <label for="editClubFocus" class="form-label fw-medium">Focus Area</label>
                                        <input type="text" class="form-control" id="editClubFocus" name="focusArea"
                                               maxlength="220" required value="<%= ViewHelper.esc(club.getFocusArea()) %>">
                                    </div>

                                    <div class="col-12">
                                        <label for="editClubDesc" class="form-label fw-medium">Description</label>
                                        <textarea class="form-control" id="editClubDesc" name="description"
                                                  rows="3" required><%= ViewHelper.esc(club.getDescription()) %></textarea>
                                    </div>

                                    <div class="col-12">
                                        <label for="editClubAct" class="form-label fw-medium">Activities</label>
                                        <textarea class="form-control" id="editClubAct" name="activities"
                                                  rows="2" required><%= ViewHelper.esc(club.getActivities()) %></textarea>
                                    </div>

                                    <% if (isAdmin) { %>
                                        <div class="col-md-6">
                                            <label for="editClubCoord" class="form-label fw-medium">Faculty Coordinator</label>
                                            <select class="form-select" id="editClubCoord" name="coordinatorId">
                                                <option value="">-- Unassigned --</option>
                                                <% if (facultyOptions != null) {
                                                    for (User f : facultyOptions) {
                                                        boolean sel = club.getCoordinatorId() != null && club.getCoordinatorId().equals(f.getUserId());
                                                %>
                                                    <option value="<%= f.getUserId() %>" <%= sel ? "selected" : "" %>>
                                                        <%= ViewHelper.esc(f.getFullName()) %> (<%= ViewHelper.esc(f.getRoleDisplayName()) %>)
                                                    </option>
                                                <%  }
                                                   } %>
                                            </select>
                                        </div>

                                        <div class="col-md-6">
                                            <label for="editClubLeader" class="form-label fw-medium">Student Club Leader</label>
                                            <select class="form-select" id="editClubLeader" name="leaderId">
                                                <option value="">-- Unassigned --</option>
                                                <% if (leaderOptions != null) {
                                                    for (User l : leaderOptions) {
                                                        boolean sel = club.getLeaderId() != null && club.getLeaderId().equals(l.getUserId());
                                                %>
                                                    <option value="<%= l.getUserId() %>" <%= sel ? "selected" : "" %>>
                                                        <%= ViewHelper.esc(l.getFullName()) %> (<%= ViewHelper.esc(l.getEmail()) %>)
                                                    </option>
                                                <%  }
                                                   } %>
                                            </select>
                                        </div>
                                    <% } %>
                                </div>
                            </div>

                            <div class="modal-footer border-top-0">
                                <button type="button" class="btn btn-outline-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancel</button>
                                <button type="submit" class="btn btn-cf-primary rounded-pill px-4">Save Changes</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        <% } %>
    <% } %>
</main>

<%@ include file="/WEB-INF/fragments/footer.jspf" %>
