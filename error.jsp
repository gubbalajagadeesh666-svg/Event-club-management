<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ page import="com.campusflow.util.AuthUtil" %>
<%@ page import="com.campusflow.util.ViewHelper" %>
<%
    int statusCode = response.getStatus();
    String errTitle = (String) request.getAttribute("errorTitle");
    String errMsg = (String) request.getAttribute("errorMessage");

    if (errTitle == null) {
        if (statusCode == 403) {
            errTitle = "403 — Access Denied";
        } else if (statusCode == 404) {
            errTitle = "404 — Page Not Found";
        } else {
            errTitle = "Something Went Wrong";
        }
    }
    if (errMsg == null) {
        if (statusCode == 403) {
            errMsg = "You do not have permission to perform this action.";
        } else if (statusCode == 404) {
            errMsg = "The requested page or resource does not exist.";
        } else {
            errMsg = "An unexpected server error occurred while processing your request.";
        }
    }

    request.setAttribute("pageTitle", errTitle);
    request.setAttribute("activeNav", "");
    boolean errLoggedIn = AuthUtil.isAuthenticated(request);
%>
<%@ include file="/WEB-INF/fragments/head.jspf" %>
<%@ include file="/WEB-INF/fragments/navbar.jspf" %>

<main class="container py-5">
    <div class="cf-card p-4 p-md-5 text-center mx-auto cf-animate-in" style="max-width: 580px;">
        <div class="cf-stat-icon bg-danger bg-opacity-10 text-danger mx-auto mb-3" style="width:64px;height:64px;font-size:1.85rem;">
            <i class="bi <%= statusCode == 403 ? "bi-shield-x" : (statusCode == 404 ? "bi-compass" : "bi-exclamation-triangle") %>"></i>
        </div>
        <h1 class="h3 fw-bold mb-2"><%= ViewHelper.esc(errTitle) %></h1>
        <p class="text-body-secondary mb-4"><%= ViewHelper.esc(errMsg) %></p>
        <div class="d-flex justify-content-center gap-2">
            <a href="<%= ctx %>/<%= errLoggedIn ? "dashboard" : "home" %>" class="btn btn-cf-primary rounded-pill px-4">
                <i class="bi bi-arrow-left me-1"></i>Return to <%= errLoggedIn ? "Dashboard" : "Home" %>
            </a>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/fragments/footer.jspf" %>
