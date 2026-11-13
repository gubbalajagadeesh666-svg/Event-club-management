<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.campusflow.util.ViewHelper" %>
<%
    request.setAttribute("pageTitle", "Student Registration");
    request.setAttribute("activeNav", "");
    String formError = (String) request.getAttribute("formError");
%>
<%@ include file="/WEB-INF/fragments/head.jspf" %>
<%@ include file="/WEB-INF/fragments/navbar.jspf" %>

<main class="container py-4">
    <div class="cf-auth-shell">
        <%@ include file="/WEB-INF/fragments/flash.jspf" %>

        <div class="cf-card overflow-hidden cf-animate-in">
            <div class="row g-0">
                <div class="col-lg-5 cf-auth-aside p-4 p-md-5 d-flex flex-column justify-content-between">
                    <div>
                        <span class="badge rounded-pill bg-white bg-opacity-10 border border-light border-opacity-25 px-3 py-2 mb-3">
                            Student Membership
                        </span>
                        <h1 class="h3 fw-bold mb-3">Join Your Campus Community</h1>
                        <p class="text-white-50 mb-4">
                            Create your student account to join clubs, register for college workshops,
                            participate in Lakshya, and track your event attendance.
                        </p>
                        <ul class="list-unstyled small text-white-50 d-grid gap-2">
                            <li><i class="bi bi-check-circle-fill text-warning me-2"></i>Instant access to student club directory</li>
                            <li><i class="bi bi-check-circle-fill text-warning me-2"></i>One-click registration for eligible events</li>
                            <li><i class="bi bi-check-circle-fill text-warning me-2"></i>Encrypted BCrypt password protection</li>
                        </ul>
                    </div>
                    <div class="small text-white-50 pt-4 mt-4 border-top border-light border-opacity-25">
                        Already registered?
                        <a href="<%= ctx %>/login" class="text-warning fw-semibold text-decoration-none ms-1">
                            Log in to your account &rarr;
                        </a>
                    </div>
                </div>

                <div class="col-lg-7 p-4 p-md-5">
                    <h2 class="h4 fw-bold mb-1">Create Student Account</h2>
                    <p class="text-body-secondary small mb-4">
                        Public registration creates a verified <strong>Student</strong> account.
                    </p>

                    <% if (formError != null) { %>
                        <div class="alert alert-danger d-flex align-items-center gap-2" role="alert">
                            <i class="bi bi-exclamation-octagon-fill flex-shrink-0"></i>
                            <div><%= ViewHelper.esc(formError) %></div>
                        </div>
                    <% } %>

                    <form method="post" action="<%= ctx %>/register" class="needs-validation" novalidate>
                        <div class="mb-3">
                            <label for="regFullName" class="form-label fw-medium">Full Name</label>
                            <input type="text" class="form-control" id="regFullName" name="fullName"
                                   minlength="2" maxlength="100" required autocomplete="name"
                                   value="<%= ViewHelper.esc(request.getAttribute("submittedName")) %>"
                                   placeholder="Enter your full name">
                            <div class="invalid-feedback">Full name is required (2–100 characters).</div>
                        </div>

                        <div class="mb-3">
                            <label for="regEmail" class="form-label fw-medium">Email Address</label>
                            <input type="email" class="form-control" id="regEmail" name="email"
                                   maxlength="150" required autocomplete="email"
                                   value="<%= ViewHelper.esc(request.getAttribute("submittedEmail")) %>"
                                   placeholder="student@college.edu">
                            <div class="invalid-feedback">Please enter a valid email address.</div>
                        </div>

                        <div class="mb-3">
                            <label for="regPassword" class="form-label fw-medium">Password</label>
                            <div class="input-group">
                                <input type="password" class="form-control" id="regPassword" name="password"
                                       minlength="8" maxlength="72" required autocomplete="new-password"
                                       placeholder="At least 8 characters">
                                <button class="btn btn-outline-secondary" type="button"
                                        data-toggle-password="regPassword" aria-label="Show password">
                                    <i class="bi bi-eye"></i>
                                </button>
                                <div class="invalid-feedback">Password must be at least 8 characters.</div>
                            </div>
                            <div class="form-text">Include at least 8 characters with uppercase, lowercase, and a number.</div>
                        </div>

                        <div class="mb-4">
                            <label for="regConfirmPassword" class="form-label fw-medium">Confirm Password</label>
                            <div class="input-group">
                                <input type="password" class="form-control" id="regConfirmPassword" name="confirmPassword"
                                       minlength="8" maxlength="72" required autocomplete="new-password"
                                       data-match-password="regPassword" placeholder="Re-enter your password">
                                <button class="btn btn-outline-secondary" type="button"
                                        data-toggle-password="regConfirmPassword" aria-label="Show confirm password">
                                    <i class="bi bi-eye"></i>
                                </button>
                                <div class="invalid-feedback">Confirmation password must match your password.</div>
                            </div>
                        </div>

                        <button type="submit" class="btn btn-cf-primary w-100 py-2 rounded-3 mb-3">
                            <i class="bi bi-person-check-fill me-1"></i>Create Student Account
                        </button>

                        <div class="text-center small text-body-secondary">
                            Already have an account?
                            <a href="<%= ctx %>/login" class="fw-semibold text-decoration-none">Back to Login</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/fragments/footer.jspf" %>
