package com.campusflow.dao;

import com.campusflow.config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * JDBC Data Access Object for marking and updating participant attendance without duplicates.
 */
public class AttendanceDAO {

    /**
     * Marks or updates attendance for registered participants of an event inside a transaction.
     * Uses unique constraint (event_id, user_id) with ON DUPLICATE KEY UPDATE so duplicate
     * attendance entries are never created.
     */
    public int saveAttendanceForEvent(int eventId, int markedByUserId, Map<Integer, String> attendanceByUserId)
            throws SQLException {
        if (attendanceByUserId == null || attendanceByUserId.isEmpty()) {
            return 0;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Set<Integer> registeredUsers = new HashSet<>();
                try (PreparedStatement regPs = conn.prepareStatement(
                        "SELECT user_id FROM event_registrations WHERE event_id = ? AND status = 'REGISTERED'")) {
                    regPs.setInt(1, eventId);
                    try (ResultSet rs = regPs.executeQuery()) {
                        while (rs.next()) {
                            registeredUsers.add(rs.getInt("user_id"));
                        }
                    }
                }

                String upsertSql = """
                        INSERT INTO attendance (event_id, user_id, attendance_status, marked_by, marked_at)
                        VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                        ON DUPLICATE KEY UPDATE
                            attendance_status = ?,
                            marked_by = ?,
                            marked_at = CURRENT_TIMESTAMP
                        """;

                int processed = 0;
                try (PreparedStatement ps = conn.prepareStatement(upsertSql)) {
                    for (Map.Entry<Integer, String> entry : attendanceByUserId.entrySet()) {
                        Integer uid = entry.getKey();
                        String status = entry.getValue();
                        if (uid == null || !registeredUsers.contains(uid)) {
                            continue;
                        }
                        if (!"PRESENT".equals(status) && !"ABSENT".equals(status)) {
                            continue;
                        }
                        ps.setInt(1, eventId);
                        ps.setInt(2, uid);
                        ps.setString(3, status);
                        ps.setInt(4, markedByUserId);
                        ps.setString(5, status);
                        ps.setInt(6, markedByUserId);
                        ps.addBatch();
                        processed++;
                    }
                    if (processed > 0) {
                        ps.executeBatch();
                    }
                }
                conn.commit();
                return processed;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
