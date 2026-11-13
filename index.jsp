<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.campusflow.model.Club" %>
<%@ page import="com.campusflow.model.Event" %>
<%@ page import="com.campusflow.dao.ReportDAO" %>
<%@ page import="com.campusflow.util.AuthUtil" %>
<%@ page import="com.campusflow.util.ViewHelper" %>
<%
    request.setAttribute("pageTitle", "Welcome");
    request.setAttribute("activeNav", "home");

    Boolean dbAvailableObj = (Boolean) request.getAttribute("dbAvailable");
    boolean dbAvailable = dbAvailableObj == null || dbAvailableObj;
    Boolean needsSetupObj = (Boolean) request.getAttribute("needsInitialSetup");
    boolean needsInitialSetup = needsSetupObj != null && needsSetupObj;

    @SuppressWarnings("unchecked")
    List<Club> clubs = (List<Club>) request.getAttribute("clubs");
    Event lakshya = (Event) request.getAttribute("lakshya");
    ReportDAO.SystemSummary summary = (ReportDAO.SystemSummary) request.getAttribute("summary");
    boolean isUserAuthenticated = AuthUtil.isAuthenticated(request);
%>
<%@ include file="/WEB-INF/fragments/head.jspf" %>
<%@ include file="/WEB-INF/fragments/navbar.jspf" %>

<main class="container py-4 py-lg-5">
    <%@ include file="/WEB-INF/fragments/flash.jspf" %>

    <% if (!dbAvailable) { %>
        <div class="alert alert-danger d-flex align-items-center gap-3 rounded-4 p-4 mb-4" role="alert">
            <i class="bi bi-database-exclamation fs-2"></i>
            <div>
                <h2 class="h5 fw-bold mb-1">Database Connection Unavailable</h2>
                <p class="mb-0"><%= ViewHelper.esc(request.getAttribute("dbErrorMessage")) %></p>
            </div>
        </div>
    <% } %>

    <% if (needsInitialSetup) { %>
        <div class="cf-card border-primary border-opacity-50 p-4 mb-4 cf-animate-in">
            <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3">
                <div class="d-flex align-items-start gap-3">
                    <div class="cf-stat-icon bg-primary bg-opacity-10 text-primary flex-shrink-0">
                        <i class="bi bi-shield-check"></i>
                    </div>
                    <div>
                        <span class="badge text-bg-primary mb-1">First-Time Initialization</span>
                        <h2 class="h5 fw-bold mb-1">Complete Initial Administrator Setup</h2>
                        <p class="text-body-secondary mb-0">
                            No Administrator account exists in <code>campusflow_db</code> yet. Create the initial
                            Administrator account securely to manage clubs, roles, approvals, and Lakshya details.
                        </p>
                    </div>
                </div>
                <a href="<%= ctx %>/setup" class="btn btn-cf-primary rounded-pill px-4 flex-shrink-0">
                    <i class="bi bi-person-gear me-1"></i>Initialize Admin
                </a>
            </div>
        </div>
    <% } %>

    <!-- Hero Banner -->
    <section class="cf-hero-gradient p-4 p-md-5 mb-5 cf-animate-in">
        <div class="row align-items-center g-4">
            <div class="col-lg-7">
                <span class="badge rounded-pill bg-white bg-opacity-10 border border-light border-opacity-25 px-3 py-2 mb-3">
                    <i class="bi bi-stars text-warning me-1"></i>Centralized Campus Life &amp; Governance
                </span>
                <h1 class="display-5 fw-bold mb-3">
                    Discover Clubs, Lead Initiatives &amp; Experience <span class="text-warning">Lakshya</span>
                </h1>
                <p class="lead text-white-50 mb-4">
                    One unified college portal connecting students, club leaders, faculty coordinators,
                    and administrators across Prakruthi, Spoorthi, Kruthi, Saheli, and campus-wide events.
                </p>
                <div class="d-flex flex-wrap gap-3">
                    <% if (isUserAuthenticated) { %>
                        <a href="<%= ctx %>/dashboard" class="btn btn-warning btn-lg rounded-pill px-4 fw-semibold">
                            <i class="bi bi-grid-1x2-fill me-2"></i>Go to Dashboard
                        </a>
                        <a href="<%= ctx %>/lakshya" class="btn btn-outline-light btn-lg rounded-pill px-4">
                            <i class="bi bi-stars me-2"></i>Explore Lakshya
                        </a>
                    <% } else { %>
                        <a href="<%= ctx %>/register" class="btn btn-warning btn-lg rounded-pill px-4 fw-semibold text-dark">
                            <i class="bi bi-person-plus-fill me-2"></i>Create Student Account
                        </a>
                        <a href="<%= ctx %>/login" class="btn btn-outline-light btn-lg rounded-pill px-4">
                            <i class="bi bi-box-arrow-in-right me-2"></i>Member Sign In
                        </a>
                    <% } %>
                </div>
            </div>

            <div class="col-lg-5">
                <div class="row g-3">
                    <div class="col-6">
                        <div class="lakshya-glass-tile text-center">
                            <div class="fs-2 fw-bold text-warning"><%= summary != null ? summary.totalClubs() : 0 %></div>
                            <div class="small text-white-50">Active Campus Clubs</div>
                        </div>
                    </div>
                    <div class="col-6">
                        <div class="lakshya-glass-tile text-center">
                            <div class="fs-2 fw-bold text-info"><%= summary != null ? summary.totalEvents() : 0 %></div>
                            <div class="small text-white-50">Recorded Events</div>
                        </div>
                    </div>
                    <div class="col-6">
                        <div class="lakshya-glass-tile text-center">
                            <div class="fs-2 fw-bold text-white"><%= summary != null ? summary.totalRegistrations() : 0 %></div>
                            <div class="small text-white-50">Event Registrations</div>
                        </div>
                    </div>
                    <div class="col-6">
                        <div class="lakshya-glass-tile text-center">
                            <div class="fs-2 fw-bold text-success"><%= summary != null ? summary.activeUsers() : 0 %></div>
                            <div class="small text-white-50">Registered Members</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Clubs Showcase (Dynamic from MySQL) -->
    <section class="mb-5">
        <div class="d-flex flex-column flex-sm-row justify-content-between align-items-sm-end mb-4 gap-2">
            <div>
                <span class="text-uppercase small fw-bold text-primary">Student Communities</span>
                <h2 class="h3 fw-bold mb-0">Official Campus Clubs</h2>
            </div>
            <a href="<%= ctx %>/<%= isUserAuthenticated ? "clubs" : "login" %>" class="btn btn-sm btn-outline-primary rounded-pill px-3">
                View All Clubs <i class="bi bi-arrow-right ms-1"></i>
            </a>
        </div>

        <div class="row g-4">
            <% if (clubs != null && !clubs.isEmpty()) {
                for (Club club : clubs) { %>
                <div class="col-md-6 col-xl-3">
                    <article class="cf-card cf-card-hover h-100 d-flex flex-column club-accent-<%= ViewHelper.esc(club.getAccentTheme()) %> cf-animate-in">
                        <div class="club-banner-strip"></div>
                        <div class="p-4 d-flex flex-column flex-grow-1">
                            <div class="d-flex align-items-center justify-content-between mb-3">
                                <div class="club-icon-box" aria-hidden="true">
                                    <i class="bi <%= ViewHelper.esc(club.getIconClass()) %>"></i>
                                </div>
                                <span class="club-tag-pill">
                                    <i class="bi bi-people-fill"></i> <%= club.getMemberCount() %> members
                                </span>
                            </div>
                            <h3 class="h5 fw-bold mb-2"><%= ViewHelper.esc(club.getClubName()) %></h3>
                            <p class="small text-body-secondary mb-3 flex-grow-1"><%= ViewHelper.esc(club.getFocusArea()) %></p>
                            <div class="pt-3 border-top d-flex justify-content-between align-items-center small">
                                <span class="text-body-secondary">
                                    <i class="bi bi-calendar2-check me-1"></i><%= club.getEventCount() %> event(s)
                                </span>
                                <a href="<%= ctx %>/<%= isUserAuthenticated ? ("clubs?id=" + club.getClubId()) : "login" %>"
                                   class="fw-semibold text-decoration-none">
                                    Explore <i class="bi bi-chevron-right"></i>
                                </a>
                            </div>
                        </div>
                    </article>
                </div>
            <%  }
               } else { %>
                <div class="col-12">
                    <div class="cf-card p-4 text-center text-body-secondary">
                        No clubs found in database yet. Run <code>database/schema.sql</code> to seed Prakruthi, Spoorthi, Kruthi, and Saheli.
                    </div>
                </div>
            <% } %>
        </div>
    </section>

    <!-- Flagship Lakshya Spotlight (Dynamic from MySQL, zero fabricated dates/venues) -->
    <% if (lakshya != null) { %>
    <section class="lakshya-spotlight p-4 p-md-5 cf-animate-in">
        <div class="row align-items-center g-4">
            <div class="col-lg-8">
                <div class="d-inline-flex align-items-center gap-2 px-3 py-1 rounded-pill bg-warning text-dark fw-bold small mb-3">
                    <i class="bi bi-trophy-fill"></i> PRIMARY COLLEGE EVENT
                </div>
                <h2 class="display-6 fw-bold mb-2"><%= ViewHelper.esc(lakshya.getEventName()) %></h2>
                <p class="text-white-50 mb-4"><%= ViewHelper.esc(lakshya.getDescription()) %></p>

                <div class="d-flex flex-wrap gap-2 mb-4">
                    <% for (Club ac : lakshya.getAssociatedClubs()) { %>
                        <span class="badge rounded-pill bg-white bg-opacity-10 border border-light border-opacity-25 px-3 py-2">
                            <i class="bi <%= ViewHelper.esc(ac.getIconClass()) %> me-1 text-warning"></i>
                            <%= ViewHelper.esc(ac.getClubName()) %>
                        </span>
                    <% } %>
                </div>

                <div class="row g-3">
                    <div class="col-sm-4">
                        <div class="lakshya-glass-tile">
                            <div class="small text-white-50"><i class="bi bi-calendar3 me-1"></i>Official Date</div>
                            <div class="fw-semibold mt-1">
                                <%= ViewHelper.esc(ViewHelper.formatDate(lakshya.getEventDate(), "To be announced")) %>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-4">
                        <div class="lakshya-glass-tile">
                            <div class="small text-white-50"><i class="bi bi-geo-alt-fill me-1"></i>Venue</div>
                            <div class="fw-semibold mt-1">
                                <%= lakshya.getVenue() != null ? ViewHelper.esc(lakshya.getVenue()) : "To be announced" %>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-4">
                        <div class="lakshya-glass-tile">
                            <div class="small text-white-50"><i class="bi bi-info-circle-fill me-1"></i>Status</div>
                            <div class="fw-semibold mt-1 text-warning">
                                <%= ViewHelper.esc(lakshya.getStatusDisplayName()) %>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-lg-4 text-lg-end">
                <div class="lakshya-glass-tile text-center p-4">
                    <div class="display-6 fw-bold text-warning mb-1"><%= lakshya.getRegisteredCount() %></div>
                    <div class="small text-white-50 mb-3">Registered Student Participants</div>
                    <a href="<%= ctx %>/<%= isUserAuthenticated ? "lakshya" : "login" %>"
                       class="btn btn-warning w-100 rounded-pill fw-semibold text-dark">
                        <i class="bi bi-arrow-up-right-circle-fill me-1"></i>Open Lakshya Page
                    </a>
                </div>
            </div>
        </div>
    </section>
    <% } %>
</main>

<%@ include file="/WEB-INF/fragments/footer.jspf" %>
