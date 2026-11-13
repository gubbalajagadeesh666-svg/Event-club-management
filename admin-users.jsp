<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.campusflow.model.User" %>
<%@ page import="com.campusflow.util.AuthUtil" %>
<%@ page import="com.campusflow.util.ViewHelper" %>
<%
    request.setAttribute("pageTitle", "Users & Role Administration");
    request.setAttribute("activeNav", "adminUsers");

    Integer signedInAdminId = AuthUtil.getUserId(request);
    @SuppressWarnings("unchecked")
    List<User> users = (List<User>) request.getAttribute("users");
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
                    <li class="breadcrumb-item active" aria-current="page">Users &amp; Roles</li>
                </ol>
            </nav>
            <h1 class="h3 fw-bold mb-1">User Accounts &amp; Role Administration</h1>
            <p class="text-body-secondary mb-0">
                Create Faculty Coordinator or Club Leader accounts, assign roles, and manage account status.
            </p>
        </div>

        <button type="button" class="btn btn-cf-primary rounded-pill px-4"
                data-bs-toggle="modal" data-bs-target="#createUserModal">
            <i class="bi bi-person-plus-fill me-1"></i>Create Account
        </button>
    </div>

    <section class="cf-card p-4 cf-animate-in">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Full Name</th>
                        <th>Email</th>
                        <th>Created</th>
                        <th>Role &amp; Account Status</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (users != null && !users.isEmpty()) {
                        for (User u : users) {
                            boolean isSelf = signedInAdminId != null && signedInAdminId.equals(u.getUserId());
                    %>
                        <tr>
                            <td class="text-body-secondary small">#<%= u.getUserId() %></td>
                            <td class="fw-semibold">
                                <%= ViewHelper.esc(u.getFullName()) %>
                                <% if (isSelf) { %>
                                    <span class="badge text-bg-primary ms-1">You</span>
                                <% } %>
                            </td>
                            <td><%= ViewHelper.esc(u.getEmail()) %></td>
                            <td class="small text-body-secondary"><%= ViewHelper.esc(ViewHelper.formatDate(u.getCreatedAt() != null ? u.getCreatedAt().toLocalDate() : null, "-")) %></td>
                            <td>
                                <form method="post" action="<%= ctx %>/admin/users" class="d-flex flex-wrap align-items-center gap-2">
                                    <input type="hidden" name="_csrf" value="<%= ViewHelper.csrf(request) %>">
                                    <input type="hidden" name="action" value="updateUser">
                                    <input type="hidden" name="userId" value="<%= u.getUserId() %>">

                                    <select class="form-select form-select-sm" style="width:auto;" name="role"
                                            aria-label="Role for <%= ViewHelper.esc(u.getFullName()) %>"
                                            <%= isSelf ? "disabled" : "" %>>
                                        <option value="STUDENT" <%= User.ROLE_STUDENT.equals(u.getRole()) ? "selected" : "" %>>Student</option>
                                        <option value="CLUB_LEADER" <%= User.ROLE_CLUB_LEADER.equals(u.getRole()) ? "selected" : "" %>>Club Leader</option>
                                        <option value="FACULTY_COORDINATOR" <%= User.ROLE_FACULTY.equals(u.getRole()) ? "selected" : "" %>>Faculty Coordinator</option>
                                        <option value="ADMIN" <%= User.ROLE_ADMIN.equals(u.getRole()) ? "selected" : "" %>>Administrator</option>
                                    </select>

                                    <select class="form-select form-select-sm" style="width:auto;" name="status"
                                            aria-label="Status for <%= ViewHelper.esc(u.getFullName()) %>"
                                            <%= isSelf ? "disabled" : "" %>>
                                        <option value="ACTIVE" <%= User.STATUS_ACTIVE.equals(u.getStatus()) ? "selected" : "" %>>Active</option>
                                        <option value="INACTIVE" <%= User.STATUS_INACTIVE.equals(u.getStatus()) ? "selected" : "" %>>Inactive</option>
                                    </select>

                                    <% if (!isSelf) { %>
                                        <button type="submit" class="btn btn-sm btn-outline-primary rounded-pill px-3">
                                            Save
                                        </button>
                                    <% } %>
                                </form>
                            </td>
                        </tr>
                    <%  }
                       } else { %>
                        <tr>
                            <td colspan="5" class="text-center text-body-secondary py-4">No user accounts found.</td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </section>

    <!-- Admin Create Account Modal -->
    <div class="modal fade" id="createUserModal" tabindex="-1" aria-labelledby="createUserModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content rounded-4 border-0 shadow">
                <form method="post" action="<%= ctx %>/admin/users" class="needs-validation" novalidate>
                    <input type="hidden" name="_csrf" value="<%= ViewHelper.csrf(request) %>">
                    <input type="hidden" name="action" value="createUser">

                    <div class="modal-header border-bottom-0 pb-0">
                        <h2 class="modal-title h5 fw-bold" id="createUserModalLabel">
                            <i class="bi bi-person-plus-fill text-primary me-2"></i>Create User Account
                        </h2>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>

                    <div class="modal-body">
                        <div class="mb-3">
                            <label for="admFullName" class="form-label fw-medium">Full Name</label>
                            <input type="text" class="form-control" id="admFullName" name="fullName"
                                   minlength="2" maxlength="100" required placeholder="e.g., Prof. Suresh Kumar">
                        </div>
                        <div class="mb-3">
                            <label for="admEmail" class="form-label fw-medium">Email Address</label>
                            <input type="email" class="form-control" id="admEmail" name="email"
                                   maxlength="150" required placeholder="faculty@college.edu">
                        </div>
                        <div class="mb-3">
                            <label for="admRole" class="form-label fw-medium">Assigned Role</label>
                            <select class="form-select" id="admRole" name="role" required>
                                <option value="FACULTY_COORDINATOR">Faculty Coordinator</option>
                                <option value="CLUB_LEADER">Club Leader</option>
                                <option value="STUDENT">Student</option>
                                <option value="ADMIN">Administrator</option>
                            </select>
                        </div>
                        <div class="mb-2">
                            <label for="admPassword" class="form-label fw-medium">Initial Password</label>
                            <div class="input-group">
                                <input type="password" class="form-control" id="admPassword" name="password"
                                       minlength="8" maxlength="72" required placeholder="Min 8 chars, upper, lower &amp; number">
                                <button class="btn btn-outline-secondary" type="button"
                                        data-toggle-password="admPassword" aria-label="Show password">
                                    <i class="bi bi-eye"></i>
                                </button>
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer border-top-0">
                        <button type="button" class="btn btn-outline-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-cf-primary rounded-pill px-4">Create Account</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/fragments/footer.jspf" %>
