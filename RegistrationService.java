package com.campusflow.service;

import com.campusflow.config.DBConnection;
import com.campusflow.model.Event;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transactional service for student event registrations.
 * Protects against duplicate registrations, expired deadlines, invalid statuses,
 * and concurrent race conditions against event capacity limits.
 */
public class RegistrationService {

    public record Outcome(boolean success, String message) {
    }

    public Outcome registerStudent(int eventId, int studentUserId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String lockEventSql = """
                        SELECT event_name, status, event_date, registration_deadline, capacity
                        FROM events
                        WHERE event_id = ?
                        FOR UPDATE
                        """;
                String status;
                LocalDate eventDate = null;
                LocalDateTime deadline = null;
                Integer capacity = null;

                try (PreparedStatement ps = conn.prepareStatement(lockEventSql)) {
                    ps.setInt(1, eventId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return new Outcome(false, "Selected event does not exist.");
                        }
                        status = rs.getString("status");
                        Date d = rs.getDate("event_date");
                        if (d != null) {
                            eventDate = d.toLocalDate();
                        }
                        Timestamp dl = rs.getTimestamp("registration_deadline");
                        if (dl != null) {
                            deadline = dl.toLocalDateTime();
                        }
                        int cap = rs.getInt("capacity");
                        if (!rs.wasNull()) {
                            capacity = cap;
                        }
                    }
                }

                if (!Event.STATUS_APPROVED.equals(status)) {
                    conn.rollback();
                    return new Outcome(false, "Registration is closed for this event.");
                }
                if (eventDate == null) {
                    conn.rollback();
                    return new Outcome(false, "Registration will open once the official event date is published.");
                }
                if (eventDate.isBefore(LocalDate.now())) {
                    conn.rollback();
                    return new Outcome(false, "Registration is closed because this event has already passed.");
                }
                if (deadline != null && LocalDateTime.now().isAfter(deadline)) {
                    conn.rollback();
                    return new Outcome(false, "Registration is closed (deadline has passed).");
                }

                String existingStatus = null;
                try (PreparedStatement checkRegPs = conn.prepareStatement(
                        "SELECT status FROM event_registrations WHERE event_id = ? AND user_id = ? FOR UPDATE")) {
                    checkRegPs.setInt(1, eventId);
                    checkRegPs.setInt(2, studentUserId);
                    try (ResultSet rs = checkRegPs.executeQuery()) {
                        if (rs.next()) {
                            existingStatus = rs.getString("status");
                        }
                    }
                }

                if ("REGISTERED".equals(existingStatus)) {
                    conn.rollback();
                    return new Outcome(false, "Already registered for this event.");
                }

                if (capacity != null) {
                    int currentRegistered = 0;
                    try (PreparedStatement countPs = conn.prepareStatement(
                            "SELECT COUNT(*) FROM event_registrations WHERE event_id = ? AND status = 'REGISTERED'")) {
                        countPs.setInt(1, eventId);
                        try (ResultSet rs = countPs.executeQuery()) {
                            if (rs.next()) {
                                currentRegistered = rs.getInt(1);
                            }
                        }
                    }
                    if (currentRegistered >= capacity) {
                        conn.rollback();
                        return new Outcome(false, "Event is full.");
                    }
                }

                if (existingStatus == null) {
                    try (PreparedStatement insPs = conn.prepareStatement(
                            "INSERT INTO event_registrations (event_id, user_id, status) VALUES (?, ?, 'REGISTERED')")) {
                        insPs.setInt(1, eventId);
                        insPs.setInt(2, studentUserId);
                        insPs.executeUpdate();
                    }
                } else {
                    try (PreparedStatement updPs = conn.prepareStatement(
                            "UPDATE event_registrations SET status = 'REGISTERED', registration_date = CURRENT_TIMESTAMP "
                            + "WHERE event_id = ? AND user_id = ?")) {
                        updPs.setInt(1, eventId);
                        updPs.setInt(2, studentUserId);
                        updPs.executeUpdate();
                    }
                }

                conn.commit();
                return new Outcome(true, "Registration completed.");
            } catch (SQLException ex) {
                conn.rollback();
                if (ex.getErrorCode() == 1062) {
                    return new Outcome(false, "Already registered for this event.");
                }
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Outcome cancelRegistration(int eventId, int studentUserId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement checkAtt = conn.prepareStatement(
                        "SELECT 1 FROM attendance WHERE event_id = ? AND user_id = ? LIMIT 1")) {
                    checkAtt.setInt(1, eventId);
                    checkAtt.setInt(2, studentUserId);
                    try (ResultSet rs = checkAtt.executeQuery()) {
                        if (rs.next()) {
                            conn.rollback();
                            return new Outcome(false, "Cannot cancel registration after attendance has been recorded.");
                        }
                    }
                }

                int updated;
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE event_registrations SET status = 'CANCELLED' "
                        + "WHERE event_id = ? AND user_id = ? AND status = 'REGISTERED'")) {
                    ps.setInt(1, eventId);
                    ps.setInt(2, studentUserId);
                    updated = ps.executeUpdate();
                }

                if (updated == 0) {
                    conn.rollback();
                    return new Outcome(false, "No active registration found to cancel.");
                }
                conn.commit();
                return new Outcome(true, "Your registration has been cancelled.");
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
