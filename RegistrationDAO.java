package com.campusflow.dao;

import com.campusflow.config.DBConnection;
import com.campusflow.model.EventRegistration;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC Data Access Object for querying student registrations and event participant rosters.
 */
public class RegistrationDAO {

    public List<EventRegistration> findByStudent(int studentUserId) throws SQLException {
        String sql = """
                SELECT er.registration_id, er.event_id, e.event_name, e.event_date, e.start_time,
                       e.venue, e.status AS event_status, er.user_id, u.full_name, u.email,
                       er.registration_date, er.status, a.attendance_status, a.marked_at
                FROM event_registrations er
                JOIN events e ON e.event_id = er.event_id
                JOIN users u ON u.user_id = er.user_id
                LEFT JOIN attendance a ON a.event_id = er.event_id AND a.user_id = er.user_id
                WHERE er.user_id = ? AND er.status = 'REGISTERED'
                ORDER BY (e.event_date IS NULL) ASC, e.event_date ASC, er.registration_date DESC
                """;
        List<EventRegistration> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRegistration(rs));
                }
            }
        }
        return list;
    }

    public List<EventRegistration> findParticipantsByEvent(int eventId) throws SQLException {
        String sql = """
                SELECT er.registration_id, er.event_id, e.event_name, e.event_date, e.start_time,
                       e.venue, e.status AS event_status, er.user_id, u.full_name, u.email,
                       er.registration_date, er.status, a.attendance_status, a.marked_at
                FROM event_registrations er
                JOIN events e ON e.event_id = er.event_id
                JOIN users u ON u.user_id = er.user_id
                LEFT JOIN attendance a ON a.event_id = er.event_id AND a.user_id = er.user_id
                WHERE er.event_id = ? AND er.status = 'REGISTERED'
                ORDER BY u.full_name ASC
                """;
        List<EventRegistration> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRegistration(rs));
                }
            }
        }
        return list;
    }

    private EventRegistration mapRegistration(ResultSet rs) throws SQLException {
        EventRegistration er = new EventRegistration();
        er.setRegistrationId(rs.getInt("registration_id"));
        er.setEventId(rs.getInt("event_id"));
        er.setEventName(rs.getString("event_name"));
        Date d = rs.getDate("event_date");
        if (d != null) {
            er.setEventDate(d.toLocalDate());
        }
        Time st = rs.getTime("start_time");
        if (st != null) {
            er.setStartTime(st.toLocalTime());
        }
        er.setVenue(rs.getString("venue"));
        er.setEventStatus(rs.getString("event_status"));
        er.setUserId(rs.getInt("user_id"));
        er.setStudentName(rs.getString("full_name"));
        er.setStudentEmail(rs.getString("email"));
        Timestamp regTs = rs.getTimestamp("registration_date");
        if (regTs != null) {
            er.setRegistrationDate(regTs.toLocalDateTime());
        }
        er.setStatus(rs.getString("status"));
        er.setAttendanceStatus(rs.getString("attendance_status"));
        Timestamp markTs = rs.getTimestamp("marked_at");
        if (markTs != null) {
            er.setMarkedAt(markTs.toLocalDateTime());
        }
        return er;
    }
}
