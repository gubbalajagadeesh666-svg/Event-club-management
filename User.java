package com.campusflow.model;

import java.time.LocalDateTime;

/**
 * Represents a user account in CampusClubSystem.
 * Note: passwordHash is used strictly inside authentication DAOs/servlets and is never
 * stored in HttpSession or rendered in JSP output.
 */
public class User {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_FACULTY = "FACULTY_COORDINATOR";
    public static final String ROLE_CLUB_LEADER = "CLUB_LEADER";
    public static final String ROLE_STUDENT = "STUDENT";

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    private int userId;
    private String fullName;
    private String email;
    private String passwordHash;
    private String role;
    private String status;
    private LocalDateTime createdAt;

    public User() {
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return STATUS_ACTIVE.equalsIgnoreCase(status);
    }

    public String getRoleDisplayName() {
        if (role == null) {
            return "Student";
        }
        return switch (role) {
            case ROLE_ADMIN -> "Administrator";
            case ROLE_FACULTY -> "Faculty Coordinator";
            case ROLE_CLUB_LEADER -> "Club Leader";
            default -> "Student";
        };
    }

    @Override
    public String toString() {
        return "User{userId=" + userId + ", email='" + email + "', role='" + role + "', status='" + status + "'}";
    }
}
