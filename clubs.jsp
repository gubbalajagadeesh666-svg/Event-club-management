<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.campusflow.model.Club" %>
<%@ page import="com.campusflow.model.User" %>
<%@ page import="com.campusflow.util.AuthUtil" %>
<%@ page import="com.campusflow.util.ViewHelper" %>
<%
    request.setAttribute("pageTitle", "Campus Clubs");
    request.setAttribute("activeNav", "clubs");

    boolean isAdmin = AuthUtil.isAdmin(request);
    @SuppressWarnings("unchecked")
    List<Club> clubs = (List<Club>) request.getAttribute("clubs");
    @SuppressWarnings("unchecked")
    List<User> facultyOptions = (List<User>) request.getAttribute("facultyOptions");
    @SuppressWarnings("unchecked")
    List<User> leaderOptions = (List<User>) request.getAttribute("leaderOptions");
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
                    <li class="breadcrumb-item active" aria-current="page">Clubs</li>
                </ol>
            </nav>
            <h1 class="h3 fw-bold mb-1">College Clubs Directory</h1>
            <p class="text-body-secondary mb-0">
                Explore student-driven communities across sustainability, literacy, performing arts, and leadership.
            </p>
        </div>

        <% if (isAdmin) { %>
            <button type="button" class="btn btn-cf-primary rounded-pill px-4"
                    data-bs-toggle="modal" data-bs-target="#createClubModal">
                <i class="bi bi-plus-circle me-1"></i>Create Club
            </button>
        <% } %>
    </div>

    <div class="row g-4">
        <% if (clubs != null && !clubs.isEmpty()) {
            for (Club club : clubs) { %>
            <div class="col-md-6 col-xl-6">
                <article class="cf-card cf-card-hover h-100 d-flex flex-column club-accent-<%= ViewHelper.esc(club.getAccentTheme()) %> cf-animate-in">
                    <div class="club-banner-strip"></div>
                    <div class="p-4 d-flex flex-column flex-grow-1">
                        <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
                            <div class="d-flex align-items-center gap-3">
                                <div class="club-icon-box" aria-hidden="true">
                                    <i class="bi <%= ViewHelper.esc(club.getIconClass()) %>"></i>
                                </div>
                                <div>
                                    <h2 class="h4 fw-bold mb-1"><%= ViewHelper.esc(club.getClubName()) %></h2>
                                    <div class="d-flex flex-wrap gap-2">
                                        <span class="club-tag-pill">
                                            <i class="bi bi-people-fill"></i> <%= club.getMemberCount() %> member(s)
                                        </span>
                                        <span class="badge rounded-pill badge-soft-secondary">
                                            <i class="bi bi-calendar2-event me-1"></i><%= club.getEventCount() %> event(s)
                                        </span>
                                        <% if (club.isUserMember()) { %>
                                            <span class="badge rounded-pill badge-soft-success">
                                                <i class="bi bi-check-circle-fill me-1"></i>Joined
                                            </span>
                                        <% } %>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <p class="mb-3"><%= ViewHelper.esc(club.getDescription()) %></p>

                        <div class="p-3 rounded-3 bg-body-tertiary mb-3 small">
                            <div class="fw-semibold mb-1"><i class="bi bi-stars text-primary me-1"></i>Key Activities</div>
                            <div class="text-body-secondary"><%= ViewHelper.esc(club.getActivities()) %></div>
                        </div>

                        <div class="mt-auto pt-3 border-top d-flex flex-wrap justify-content-between align-items-center gap-2 small">
                            <div class="text-body-secondary">
                                <% if (club.getCoordinatorName() != null) { %>
                                    <span class="me-3"><i class="bi bi-person-workspace me-1"></i>Coord: <strong><%= ViewHelper.esc(club.getCoordinatorName()) %></strong></span>
                                <% } %>
                                <% if (club.getLeaderName() != null) { %>
                                    <span><i class="bi bi-award me-1"></i>Leader: <strong><%= ViewHelper.esc(club.getLeaderName()) %></strong></span>
                                <% } %>
                            </div>
                            <a href="<%= ctx %>/clubs?id=<%= club.getClubId() %>"
                               class="btn btn-sm btn-outline-primary rounded-pill px-3">
                                View Details <i class="bi bi-arrow-right ms-1"></i>
                            </a>
                        </div>
                    </div>
                </article>
            </div>
        <%  }
           } else { %>
            <div class="col-12">
                <div class="cf-card p-5 text-center text-body-secondary">
                    <i class="bi bi-people fs-1 d-block mb-2"></i>
                    <h2 class="h5 fw-bold text-body">No Clubs Available</h2>
                    <p class="mb-0">No club records were found in MySQL.</p>
                </div>
            </div>
        <% } %>
    </div>

    <!-- Admin-Only Create Club Modal -->
    <% if (isAdmin) { %>
    <div class="modal fade" id="createClubModal" tabindex="-1" aria-labelledby="createClubModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg modal-dialog-centered">
            <div class="modal-content rounded-4 border-0 shadow">
                <form method="post" action="<%= ctx %>/clubs" class="needs-validation" novalidate>
                    <input type="hidden" name="_csrf" value="<%= ViewHelper.csrf(request) %>">
                    <input type="hidden" name="action" value="create">

                    <div class="modal-header border-bottom-0 pb-0">
                        <h2 class="modal-title h5 fw-bold" id="createClubModalLabel">
                            <i class="bi bi-plus-circle text-primary me-2"></i>Create New Campus Club
                        </h2>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>

                    <div class="modal-body">
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label for="newClubName" class="form-label fw-medium">Club Name</label>
                                <input type="text" class="form-control" id="newClubName" name="clubName"
                                       maxlength="100" required placeholder="e.g., Robotics &amp; AI Club">
                                <div class="invalid-feedback">Club name is required.</div>
                            </div>
                            <div class="col-md-3">
                                <label for="newClubIcon" class="form-label fw-medium">Icon</label>
                                <select class="form-select" id="newClubIcon" name="iconClass">
                                    <option value="bi-stars">Stars</option>
                                    <option value="bi-cpu-fill">Technology / CPU</option>
                                    <option value="bi-lightbulb-fill">Innovation</option>
                                    <option value="bi-camera-reels-fill">Media / Film</option>
                                    <option value="bi-trophy-fill">Sports / Trophy</option>
                                    <option value="bi-globe-central-south-asia">Global / Social</option>
                                    <option value="bi-tree-fill">Environment</option>
                                    <option value="bi-journal-richtext">Literary</option>
                                    <option value="bi-music-note-beamed">Performing Arts</option>
                                    <option value="bi-heart-pulse-fill">Wellness / Empowerment</option>
                                </select>
                            </div>
                            <div class="col-md-3">
                                <label for="newClubTheme" class="form-label fw-medium">Color Accent</label>
                                <select class="form-select" id="newClubTheme" name="accentTheme">
                                    <option value="indigo">Indigo</option>
                                    <option value="emerald">Emerald Green</option>
                                    <option value="sky">Sky Blue</option>
                                    <option value="amber">Warm Amber</option>
                                    <option value="rose">Rose Crimson</option>
                                </select>
                            </div>

                            <div class="col-12">
                                <label for="newClubFocus" class="form-label fw-medium">Focus Summary</label>
                                <input type="text" class="form-control" id="newClubFocus" name="focusArea"
                                       maxlength="220" required
                                       placeholder="Short summary of the club's core mission">
                                <div class="invalid-feedback">Focus summary is required.</div>
                            </div>

                            <div class="col-12">
                                <label for="newClubDesc" class="form-label fw-medium">Full Description</label>
                                <textarea class="form-control" id="newClubDesc" name="description" rows="3"
                                          required placeholder="Describe what students learn and achieve in this club"></textarea>
                                <div class="invalid-feedback">Description is required.</div>
                            </div>

                            <div class="col-12">
                                <label for="newClubActivities" class="form-label fw-medium">Typical Activities</label>
                                <textarea class="form-control" id="newClubActivities" name="activities" rows="2"
                                          required placeholder="Workshops, competitions, community projects..."></textarea>
                                <div class="invalid-feedback">Activities summary is required.</div>
                            </div>

                            <div class="col-md-6">
                                <label for="newClubCoord" class="form-label fw-medium">Faculty Coordinator (Optional)</label>
                                <select class="form-select" id="newClubCoord" name="coordinatorId">
                                    <option value="">-- Unassigned --</option>
                                    <% if (facultyOptions != null) {
                                        for (User f : facultyOptions) { %>
                                            <option value="<%= f.getUserId() %>">
                                                <%= ViewHelper.esc(f.getFullName()) %> (<%= ViewHelper.esc(f.getEmail()) %>)
                                            </option>
                                    <%  }
                                       } %>
                                </select>
                            </div>

                            <div class="col-md-6">
                                <label for="newClubLeader" class="form-label fw-medium">Club Leader (Optional)</label>
                                <select class="form-select" id="newClubLeader" name="leaderId">
                                    <option value="">-- Unassigned --</option>
                                    <% if (leaderOptions != null) {
                                        for (User l : leaderOptions) { %>
                                            <option value="<%= l.getUserId() %>">
                                                <%= ViewHelper.esc(l.getFullName()) %> (<%= ViewHelper.esc(l.getEmail()) %>)
                                            </option>
                                    <%  }
                                       } %>
                                </select>
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer border-top-0">
                        <button type="button" class="btn btn-outline-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-cf-primary rounded-pill px-4">Create Club</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <% } %>
</main>

<%@ include file="/WEB-INF/fragments/footer.jspf" %>
