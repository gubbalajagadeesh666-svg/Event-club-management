<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.campusflow.util.ViewHelper" %>
<%
    request.setAttribute("pageTitle", "Initial Administrator Setup");
    request.setAttribute("activeNav", "");
    String formError = (String) request.getAttribute("formError");
%>
<%@ include file="/WEB-INF/fragments/head.jspf" %>
<%@ include file="/WEB-INF/fragments/navbar.jspf" %>

<main class="container py-4">
    <div class="cf-auth-shell cf-card overflow-hidden cf-animate-in">
        <div class="row g-0">
            <div class="col-lg-5 cf-auth-aside p-4 p-md-5 d-flex flex-column justify-content-between">
                <div>
                    <span class="badge rounded-pill bg-warning text-dark fw-semibold px-3 py-2 mb-3">
                        <i class="bi bi-shield-lock-fill me-1"></i>One-Time Bootstrap
                    </span>
                    <h1 class="h3 fw-bold mb-3">Initialize Campus Administrator</h1>
                    <p class="text-white-50 mb-4">
                        For security, <code>schema.sql</code> ships with zero pre-seeded passwords. Use this one-time
                        controlled setup to create the primary Administrator account.
                    </p>
                    <ul class="list-unstyled small text-white-50 d-grid gap-2">
                        <li><i class="bi bi-check2-circle text-warning me-2"></i>Password hashed with BCrypt (cost 12)</li>
                        <li><i class="bi bi-check2-circle text-warning me-2"></i>Automatically locks once 1st Admin exists</li>
                        <li><i class="bi bi-check2-circle text-warning me-2"></i>Create Faculty &amp; Club Leaders afterward</li>
                    </ul>
                </div>
                <div class="small text-white-50 pt-4 border-top border-light border-opacity-25">
                    Already initialized? <a href="<%= ctx %>/login" class="text-warning fw-semibold">Go to Login</a>
                </div>
            </div>

            <div class="col-lg-7 p-4 p-md-5">
                <h2 class="h4 fw-bold mb-1">Create First Administrator</h2>
                <p class="text-body-secondary small mb-4">All fields are validated on the server before saving to MySQL.</p>

                <% if (formError != null) { %>
                    <div class="alert alert-danger d-flex align-items-center gap-2" role="alert">
                        <i class="bi bi-exclamation-octagon-fill flex-shrink-0"></i>
                        <div><%= ViewHelper.esc(formError) %></div>
                    </div>
                <% } %>

                <form method="post" action="<%= ctx %>/setup" class="needs-validation" novalidate>
                    <input type="hidden" name="_csrf" value="<%= ViewHelper.csrf(request) %>">

                    <div class="mb-3">
                        <label for="setupFullName" class="form-label fw-medium">Administrator Full Name</label>
                        <input type="text" class="form-control" id="setupFullName" name="fullName"
                               maxlength="100" minlength="2" required
                               value="<%= ViewHelper.esc(request.getAttribute("submittedName")) %>"
                               placeholder="e.g., Dr. Ananya Rao">
                        <div class="invalid-feedback">Please enter the administrator's full name (2–100 characters).</div>
                    </div>

                    <div class="mb-3">
                        <label for="setupEmail" class="form-label fw-medium">Official Email Address</label>
                        <input type="email" class="form-control" id="setupEmail" name="email"
                               maxlength="150" required
                               value="<%= ViewHelper.esc(request.getAttribute("submittedEmail")) %>"
                               placeholder="admin@college.edu">
                        <div class="invalid-feedback">Please enter a valid email address.</div>
                    </div>

                    <div class="mb-3">
                        <label for="setupPassword" class="form-label fw-medium">Password</label>
                        <div class="input-group">
                            <input type="password" class="form-control" id="setupPassword" name="password"
                                   minlength="8" maxlength="72" required
                                   placeholder="Minimum 8 chars, upper, lower &amp; number">
                            <button class="btn btn-outline-secondary" type="button"
                                    data-toggle-password="setupPassword" aria-label="Show password">
                                <i class="bi bi-eye"></i>
                            </button>
                            <div class="invalid-feedback">Minimum 8 characters with uppercase, lowercase, and a number.</div>
                        </div>
                        <div class="form-text">Must contain at least 8 characters, 1 uppercase, 1 lowercase, and 1 digit.</div>
                    </div>

                    <div class="mb-4">
                        <label for="setupConfirmPassword" class="form-label fw-medium">Confirm Password</label>
                        <div class="input-group">
                            <input type="password" class="form-control" id="setupConfirmPassword" name="confirmPassword"
                                   minlength="8" maxlength="72" required data-match-password="setupPassword"
                                   placeholder="Re-enter password">
                            <button class="btn btn-outline-secondary" type="button"
                                    data-toggle-password="setupConfirmPassword" aria-label="Show confirm password">
                                <i class="bi bi-eye"></i>
                            </button>
                            <div class="invalid-feedback">Confirmation password must match your password.</div>
                        </div>
                    </div>

                    <button type="submit" class="btn btn-cf-primary w-100 py-2 rounded-3">
                        <i class="bi bi-shield-check me-1"></i>Create Administrator Account
                    </button>
                </form>
            </div>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/fragments/footer.jspf" %>
