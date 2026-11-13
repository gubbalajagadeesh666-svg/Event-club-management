package com.campusflow.service;

import com.campusflow.config.DBConnection;
import com.campusflow.model.Event;
import com.campusflow.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Transactional service enforcing event proposal approval and rejection rules.
 */
public class EventApprovalService {

    public record Outcome(boolean success, String message) {
    }

    public Outcome reviewProposal(int eventId, int reviewerId, String reviewerRole,
                                  String decision, String remarks) throws SQLException {
        if (!User.ROLE_ADMIN.equals(reviewerRole) && !User.ROLE_FACULTY.equals(reviewerRole)) {
            return new Outcome(false, "You do not have permission to perform this action.");
        }
        if (!"APPROVED".equals(decision) && !"REJECTED".equals(decision)) {
            return new Outcome(false, "Invalid approval decision.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String currentStatus;
                Integer createdBy = null;
                try (PreparedStatement lockPs = conn.prepareStatement(
                        "SELECT status, created_by FROM events WHERE event_id = ? FOR UPDATE")) {
                    lockPs.setInt(1, eventId);
                    try (ResultSet rs = lockPs.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return new Outcome(false, "Event proposal not found.");
                        }
                        currentStatus = rs.getString("status");
                        int cb = rs.getInt("created_by");
                        if (!rs.wasNull()) {
                            createdBy = cb;
                        }
                    }
                }

                if (!Event.STATUS_PENDING_APPROVAL.equals(currentStatus)) {
                    conn.rollback();
                    return new Outcome(false, "This event proposal is no longer awaiting approval.");
                }

                // Prevent self-approval by non-Administrators
                if (!User.ROLE_ADMIN.equals(reviewerRole) && createdBy != null && createdBy == reviewerId) {
                    conn.rollback();
                    return new Outcome(false, "Self-approval of your own event proposal is not permitted.");
                }

                // Faculty Coordinator must be assigned to at least one club associated with the event
                if (User.ROLE_FACULTY.equals(reviewerRole)) {
                    String authSql = """
                            SELECT 1 FROM event_clubs ec
                            JOIN clubs c ON c.club_id = ec.club_id
                            WHERE ec.event_id = ? AND c.coordinator_id = ?
                            LIMIT 1
                            """;
                    try (PreparedStatement authPs = conn.prepareStatement(authSql)) {
                        authPs.setInt(1, eventId);
                        authPs.setInt(2, reviewerId);
                        try (ResultSet rs = authPs.executeQuery()) {
                            if (!rs.next()) {
                                conn.rollback();
                                return new Outcome(false, "You can only review proposals for clubs assigned to you.");
                            }
                        }
                    }
                }

                try (PreparedStatement updPs = conn.prepareStatement(
                        "UPDATE events SET status = ? WHERE event_id = ?")) {
                    updPs.setString(1, decision);
                    updPs.setInt(2, eventId);
                    updPs.executeUpdate();
                }

                try (PreparedStatement auditPs = conn.prepareStatement(
                        "INSERT INTO event_approvals (event_id, reviewer_id, previous_status, decision, remarks) "
                        + "VALUES (?, ?, ?, ?, ?)")) {
                    auditPs.setInt(1, eventId);
                    auditPs.setInt(2, reviewerId);
                    auditPs.setString(3, currentStatus);
                    auditPs.setString(4, decision);
                    auditPs.setString(5, remarks);
                    auditPs.executeUpdate();
                }

                conn.commit();
                String actionWord = "APPROVED".equals(decision) ? "approved" : "rejected";
                return new Outcome(true, "Event proposal has been " + actionWord + ".");
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
