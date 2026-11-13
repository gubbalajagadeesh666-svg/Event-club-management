<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.campusflow.util.ViewHelper" %>
<%
    request.setAttribute("pageTitle", "Sign In");
    request.setAttribute("activeNav", "");

    String formError = (String) request.getAttribute("formError");
    String infoBanner = (String) request.getAttribute("infoBanner");
    String warningBanner = (String) request.getAttribute("warningBanner");
    String dbWarning = (String) request.getAttribute("dbWarning");
    Boolean needsSetupObj = (Boolean) request.getAttribute("needsInitialSetup");
    boolean needsInitialSetup = needsSetupObj != null && needsSetupObj;
%>
<%@ include file="/WEB-INF/fragments/head.jspf" %>
<%@ include file="/WEB-INF/fragments/navbar.jspf" %>

<main class="container py-4">
    <div class="cf-auth-shell">
        <%@ include file="/WEB-INF/fragments/flash.jspf" %>

        <% if (needsInitialSetup) { %>
            <div class="alert alert-primary d-flex flex-column flex-sm-row align-items-sm-center justify-content-between gap-2 rounded-3 mb-3" role="alert">
                <div>
                    <i class="bi bi-shield-exclamation me-1"></i>
                    <strong>First-time setup available:</strong> No Administrator account has been created yet.
                </div>
                <a href="<%= ctx %>/setup" class="btn btn-sm btn-primary rounded-pill px-3 flex-shrink-0">
                    Initialize Admin
                </a>
            </div>
        <% } %>

        <div class="cf-card overflow-hidden cf-animate-in">
            <div class="row g-0">
                <div class="col-lg-5 cf-auth-aside p-4 p-md-5 d-flex flex-column justify-content-between">
                    <div>
                        <span class="badge rounded-pill bg-white bg-opacity-10 border border-light border-opacity-25 px-3 py-2 mb-3">
                            CampusClubSystem Portal
                        </span>
                        <h1 class="h3 fw-bold mb-3">Welcome Back to Campus Life</h1>
                        <p class="text-white-50 mb-4">
                            Sign in to browse clubs, register for upcoming events, view Lakshya updates,
                            or manage your assigned club responsibilities.
                        </p>
                        <div class="d-grid gap-2 small text-white-50">
                            <div><i class="bi bi-tree-fill text-success me-2"></i><strong class="text-white">Prakruthi</strong> &mdash; Sustainability &amp; Greenery</div>
                            <div><i class="bi bi-journal-richtext text-info me-2"></i><strong class="text-white">Spoorthi</strong> &mdash; Literacy &amp; Debates</div>
                            <div><i class="bi bi-music-note-beamed text-warning me-2"></i><strong class="text-white">Kruthi</strong> &mdash; Performing Arts</div>
                            <div><i class="bi bi-heart-pulse-fill text-danger me-2"></i><strong class="text-white">Saheli</strong> &mdash; Empowerment &amp; Leadership</div>
                        </div>
                    </div>
                    <div class="small text-white-50 pt-4 mt-4 border-top border-light border-opacity-25">
                        New student on campus?
                        <a href="<%= ctx %>/register" class="text-warning fw-semibold text-decoration-none ms-1">
                            Create an account &rarr;
                        </a>
                    </div>
                </div>

                <div class="col-lg-7 p-4 p-md-5">
                    <h2 class="h4 fw-bold mb-1">Account Sign In</h2>
                    <p class="text-body-secondary small mb-4">Enter your college email and password to continue.</p>

                    <% if (dbWarning != null) { %>
                        <div class="alert alert-danger d-flex align-items-center gap-2" role="alert">
                            <i class="bi bi-database-exclamation flex-shrink-0"></i>
                            <div><%= ViewHelper.esc(dbWarning) %></div>
                        </div>
                    <% } %>

                    <% if (infoBanner != null) { %>
                        <div class="alert alert-success d-flex align-items-center gap-2" role="alert">
                            <i class="bi bi-check-circle-fill flex-shrink-0"></i>
                            <div><%= ViewHelper.esc(infoBanner) %></div>
                        </div>
                    <% } %>

                    <% if (warningBanner != null) { %>
                        <div class="alert alert-warning d-flex align-items-center gap-2" role="alert">
                            <i class="bi bi-clock-history flex-shrink-0"></i>
                            <div><%= ViewHelper.esc(warningBanner) %></div>
                        </div>
                    <% } %>

                    <% if (formError != null) { %>
                        <div class="alert alert-danger d-flex align-items-center gap-2" role="alert">
                            <i class="bi bi-exclamation-octagon-fill flex-shrink-0"></i>
                            <div><%= ViewHelper.esc(formError) %></div>
                        </div>
                    <% } %>

                    <form method="post" action="<%= ctx %>/login" class="needs-validation" novalidate>
                        <div class="mb-3">
                            <label for="loginEmail" class="form-label fw-medium">Email Address</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-envelope"></i></span>
                                <input type="email" class="form-control" id="loginEmail" name="email"
                                       maxlength="150" required autocomplete="username"
                                       value="<%= ViewHelper.esc(request.getAttribute("submittedEmail")) %>"
                                       placeholder="name@college.edu">
                                <div class="invalid-feedback">Please enter a valid email address.</div>
                            </div>
                        </div>

                        <div class="mb-4">
                            <label for="loginPassword" class="form-label fw-medium">Password</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-lock"></i></span>
                                <input type="password" class="form-control" id="loginPassword" name="password"
                                       required autocomplete="current-password" placeholder="Enter your password">
                                <button class="btn btn-outline-secondary" type="button"
                                        data-toggle-password="loginPassword" aria-label="Show password">
                                    <i class="bi bi-eye"></i>
                                </button>
                                <div class="invalid-feedback">Please enter your password.</div>
                            </div>
                        </div>

                        <button type="submit" class="btn btn-cf-primary w-100 py-2 rounded-3 mb-3">
                            <i class="bi bi-box-arrow-in-right me-1"></i>Log In
                        </button>

                        <div class="text-center small text-body-secondary">
                            Don't have a student account yet?
                            <a href="<%= ctx %>/register" class="fw-semibold text-decoration-none">Register here</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/fragments/footer.jspf" %>
