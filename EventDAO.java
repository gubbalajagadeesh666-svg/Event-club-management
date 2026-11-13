package com.campusflow.dao;

import com.campusflow.config.DBConnection;
import com.campusflow.model.Club;
import com.campusflow.model.Event;
import com.campusflow.model.EventApproval;
import com.campusflow.model.User;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC Data Access Object for events, flagship Lakshya, club associations, and approval queries.
 */
public class EventDAO {

    private static final String BASE_EVENT_SELECT = """
            SELECT e.event_id, e.event_code, e.is_flagship, e.event_name, e.description,
                   e.event_date, e.start_time, e.end_time, e.venue, e.organizer,
                   e.registration_deadline, e.capacity, e.status, e.created_by,
                   u.full_name AS created_by_name, e.created_at,
                   (SELECT COUNT(*) FROM event_registrations er
                    WHERE er.event_id = e.event_id AND er.status = 'REGISTERED') AS registered_count,
                   (SELECT COUNT(*) FROM attendance a
                    WHERE a.event_id = e.event_id AND a.attendance_status = 'PRESENT') AS present_count,
                   EXISTS(SELECT 1 FROM event_registrations er2
                          WHERE er2.event_id = e.event_id AND er2.user_id = ? AND er2.status = 'REGISTERED') AS is_user_registered
            FROM events e
            LEFT JOIN users u ON u.user_id = e.created_by
            """;

    public Event findLakshya(Integer currentUserId) throws SQLException {
        String sql = BASE_EVENT_SELECT
                + " WHERE e.event_code = 'LAKSHYA' OR e.is_flagship = 1 "
                + " ORDER BY (e.event_code = 'LAKSHYA') DESC, e.is_flagship DESC, e.event_id ASC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUserId == null ? -1 : currentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Event lakshya = mapEvent(rs);
                populateAssociatedClubs(conn, List.of(lakshya));
                return lakshya;
            }
        }
    }

    public Event findById(int eventId, Integer currentUserId) throws SQLException {
        String sql = BASE_EVENT_SELECT + " WHERE e.event_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUserId == null ? -1 : currentUserId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Event event = mapEvent(rs);
                populateAssociatedClubs(conn, List.of(event));
                return event;
            }
        }
    }

    public List<Event> findEvents(String statusFilter, Integer clubIdFilter,
                                  Integer viewerUserId, String viewerRole) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_EVENT_SELECT).append(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        params.add(viewerUserId == null ? -1 : viewerUserId);

        boolean isAdmin = User.ROLE_ADMIN.equals(viewerRole);
        boolean isFaculty = User.ROLE_FACULTY.equals(viewerRole);
        boolean isLeader = User.ROLE_CLUB_LEADER.equals(viewerRole);

        if (!isAdmin) {
            if (isFaculty && viewerUserId != null) {
                sql.append("""
                        AND (e.status IN ('APPROVED', 'COMPLETED', 'SCHEDULE_PENDING')
                             OR EXISTS (SELECT 1 FROM event_clubs ec_f
                                        JOIN clubs c_f ON c_f.club_id = ec_f.club_id
                                        WHERE ec_f.event_id = e.event_id AND c_f.coordinator_id = ?))
                        """);
                params.add(viewerUserId);
            } else if (isLeader && viewerUserId != null) {
                sql.append("""
                        AND (e.status IN ('APPROVED', 'COMPLETED', 'SCHEDULE_PENDING')
                             OR e.created_by = ?
                             OR EXISTS (SELECT 1 FROM event_clubs ec_l
                                        JOIN clubs c_l ON c_l.club_id = ec_l.club_id
                                        WHERE ec_l.event_id = e.event_id AND c_l.leader_id = ?))
                        """);
                params.add(viewerUserId);
                params.add(viewerUserId);
            } else {
                sql.append(" AND e.status IN ('APPROVED', 'COMPLETED', 'SCHEDULE_PENDING') ");
            }
        }

        if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND e.status = ? ");
            params.add(statusFilter.trim());
        }

        if (clubIdFilter != null && clubIdFilter > 0) {
            sql.append(" AND EXISTS (SELECT 1 FROM event_clubs ec2 WHERE ec2.event_id = e.event_id AND ec2.club_id = ?) ");
            params.add(clubIdFilter);
        }

        sql.append(" ORDER BY e.is_flagship DESC, (e.event_date IS NULL) ASC, e.event_date ASC, e.event_id DESC");

        List<Event> events = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(mapEvent(rs));
                }
            }
            populateAssociatedClubs(conn, events);
        }
        return events;
    }

    public List<Event> findUpcomingEvents(int limit, Integer currentUserId) throws SQLException {
        String sql = BASE_EVENT_SELECT
                + " WHERE e.status = 'APPROVED' AND (e.event_date IS NULL OR e.event_date >= CURDATE()) "
                + " ORDER BY e.is_flagship DESC, e.event_date ASC, e.start_time ASC LIMIT ?";
        List<Event> events = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUserId == null ? -1 : currentUserId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(mapEvent(rs));
                }
            }
            populateAssociatedClubs(conn, events);
        }
        return events;
    }

    public List<Event> findEventsByClub(int clubId, Integer currentUserId, boolean includePending) throws SQLException {
        String statusClause = includePending
                ? ""
                : " AND e.status IN ('APPROVED', 'COMPLETED', 'SCHEDULE_PENDING') ";
        String sql = BASE_EVENT_SELECT
                + " WHERE EXISTS (SELECT 1 FROM event_clubs ec WHERE ec.event_id = e.event_id AND ec.club_id = ?) "
                + statusClause
                + " ORDER BY e.is_flagship DESC, (e.event_date IS NULL) ASC, e.event_date ASC";
        List<Event> events = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUserId == null ? -1 : currentUserId);
            ps.setInt(2, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(mapEvent(rs));
                }
            }
            populateAssociatedClubs(conn, events);
        }
        return events;
    }

    public List<Event> findPendingApprovalsForReviewer(int reviewerId, String reviewerRole) throws SQLException {
        String sql;
        boolean isAdmin = User.ROLE_ADMIN.equals(reviewerRole);
        if (isAdmin) {
            sql = BASE_EVENT_SELECT + " WHERE e.status = 'PENDING_APPROVAL' ORDER BY e.created_at ASC";
        } else if (User.ROLE_FACULTY.equals(reviewerRole)) {
            sql = BASE_EVENT_SELECT + """
                     WHERE e.status = 'PENDING_APPROVAL'
                       AND EXISTS (SELECT 1 FROM event_clubs ec
                                   JOIN clubs c ON c.club_id = ec.club_id
                                   WHERE ec.event_id = e.event_id AND c.coordinator_id = ?)
                     ORDER BY e.created_at ASC
                    """;
        } else {
            return new ArrayList<>();
        }

        List<Event> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewerId);
            if (!isAdmin) {
                ps.setInt(2, reviewerId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEvent(rs));
                }
            }
            populateAssociatedClubs(conn, list);
        }
        return list;
    }

    public int createEvent(Event event, List<Integer> clubIds) throws SQLException {
        String insertSql = """
                INSERT INTO events (event_code, is_flagship, event_name, description, event_date,
                                    start_time, end_time, venue, organizer, registration_deadline,
                                    capacity, status, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int newId;
                try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    bindEventColumns(ps, event);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Failed to obtain event_id after insert.");
                        }
                        newId = keys.getInt(1);
                    }
                }
                replaceEventClubs(conn, newId, clubIds);
                conn.commit();
                return newId;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean updateEvent(Event event, List<Integer> clubIds) throws SQLException {
        String updateSql = """
                UPDATE events
                SET event_name = ?, description = ?, event_date = ?, start_time = ?, end_time = ?,
                    venue = ?, organizer = ?, registration_deadline = ?, capacity = ?, status = ?
                WHERE event_id = ?
                """;
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int updated;
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, event.getEventName());
                    ps.setString(2, event.getDescription());
                    ps.setDate(3, event.getEventDate() == null ? null : Date.valueOf(event.getEventDate()));
                    ps.setTime(4, event.getStartTime() == null ? null : Time.valueOf(event.getStartTime()));
                    ps.setTime(5, event.getEndTime() == null ? null : Time.valueOf(event.getEndTime()));
                    ps.setString(6, event.getVenue());
                    ps.setString(7, event.getOrganizer());
                    ps.setTimestamp(8, event.getRegistrationDeadline() == null
                            ? null : Timestamp.valueOf(event.getRegistrationDeadline()));
                    if (event.getCapacity() != null) {
                        ps.setInt(9, event.getCapacity());
                    } else {
                        ps.setNull(9, Types.INTEGER);
                    }
                    ps.setString(10, event.getStatus());
                    ps.setInt(11, event.getEventId());
                    updated = ps.executeUpdate();
                }
                if (updated > 0 && clubIds != null) {
                    replaceEventClubs(conn, event.getEventId(), clubIds);
                }
                conn.commit();
                return updated > 0;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean updateEventStatus(int eventId, String status) throws SQLException {
        String sql = "UPDATE events SET status = ? WHERE event_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, eventId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean hasRegistrationsOrAttendance(int eventId) throws SQLException {
        String sql = """
                SELECT (SELECT COUNT(*) FROM event_registrations WHERE event_id = ?) +
                       (SELECT COUNT(*) FROM attendance WHERE event_id = ?)
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean deleteEvent(int eventId) throws SQLException {
        String sql = "DELETE FROM events WHERE event_id = ? AND is_flagship = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean isAuthorizedOrganizerForEvent(int eventId, int userId, String role) throws SQLException {
        if (User.ROLE_ADMIN.equals(role)) {
            return true;
        }
        String sql;
        if (User.ROLE_FACULTY.equals(role)) {
            sql = """
                    SELECT 1 FROM event_clubs ec
                    JOIN clubs c ON c.club_id = ec.club_id
                    WHERE ec.event_id = ? AND c.coordinator_id = ? LIMIT 1
                    """;
        } else if (User.ROLE_CLUB_LEADER.equals(role)) {
            sql = """
                    SELECT 1 FROM event_clubs ec
                    JOIN clubs c ON c.club_id = ec.club_id
                    WHERE ec.event_id = ? AND c.leader_id = ? LIMIT 1
                    """;
        } else {
            return false;
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<EventApproval> findApprovalHistory(int eventId) throws SQLException {
        String sql = """
                SELECT ea.approval_id, ea.event_id, e.event_name, ea.reviewer_id,
                       u.full_name AS reviewer_name, u.role AS reviewer_role,
                       ea.previous_status, ea.decision, ea.remarks, ea.reviewed_at
                FROM event_approvals ea
                JOIN events e ON e.event_id = ea.event_id
                JOIN users u ON u.user_id = ea.reviewer_id
                WHERE ea.event_id = ?
                ORDER BY ea.reviewed_at DESC
                """;
        List<EventApproval> history = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EventApproval a = new EventApproval();
                    a.setApprovalId(rs.getInt("approval_id"));
                    a.setEventId(rs.getInt("event_id"));
                    a.setEventName(rs.getString("event_name"));
                    a.setReviewerId(rs.getInt("reviewer_id"));
                    a.setReviewerName(rs.getString("reviewer_name"));
                    a.setReviewerRole(rs.getString("reviewer_role"));
                    a.setPreviousStatus(rs.getString("previous_status"));
                    a.setDecision(rs.getString("decision"));
                    a.setRemarks(rs.getString("remarks"));
                    Timestamp ts = rs.getTimestamp("reviewed_at");
                    if (ts != null) {
                        a.setReviewedAt(ts.toLocalDateTime());
                    }
                    history.add(a);
                }
            }
        }
        return history;
    }

    private void bindEventColumns(PreparedStatement ps, Event event) throws SQLException {
        ps.setString(1, event.getEventCode());
        ps.setInt(2, event.isFlagship() ? 1 : 0);
        ps.setString(3, event.getEventName());
        ps.setString(4, event.getDescription());
        ps.setDate(5, event.getEventDate() == null ? null : Date.valueOf(event.getEventDate()));
        ps.setTime(6, event.getStartTime() == null ? null : Time.valueOf(event.getStartTime()));
        ps.setTime(7, event.getEndTime() == null ? null : Time.valueOf(event.getEndTime()));
        ps.setString(8, event.getVenue());
        ps.setString(9, event.getOrganizer());
        ps.setTimestamp(10, event.getRegistrationDeadline() == null
                ? null : Timestamp.valueOf(event.getRegistrationDeadline()));
        if (event.getCapacity() != null) {
            ps.setInt(11, event.getCapacity());
        } else {
            ps.setNull(11, Types.INTEGER);
        }
        ps.setString(12, event.getStatus());
        if (event.getCreatedBy() != null) {
            ps.setInt(13, event.getCreatedBy());
        } else {
            ps.setNull(13, Types.INTEGER);
        }
    }

    private void replaceEventClubs(Connection conn, int eventId, List<Integer> clubIds) throws SQLException {
        try (PreparedStatement delPs = conn.prepareStatement("DELETE FROM event_clubs WHERE event_id = ?")) {
            delPs.setInt(1, eventId);
            delPs.executeUpdate();
        }
        if (clubIds == null || clubIds.isEmpty()) {
            return;
        }
        try (PreparedStatement insPs = conn.prepareStatement(
                "INSERT IGNORE INTO event_clubs (event_id, club_id) VALUES (?, ?)")) {
            for (Integer clubId : clubIds) {
                if (clubId != null && clubId > 0) {
                    insPs.setInt(1, eventId);
                    insPs.setInt(2, clubId);
                    insPs.addBatch();
                }
            }
            insPs.executeBatch();
        }
    }

    private void populateAssociatedClubs(Connection conn, List<Event> events) throws SQLException {
        if (events == null || events.isEmpty()) {
            return;
        }
        Map<Integer, Event> byId = new LinkedHashMap<>();
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < events.size(); i++) {
            Event ev = events.get(i);
            ev.setAssociatedClubs(new ArrayList<>());
            byId.put(ev.getEventId(), ev);
            if (i > 0) {
                placeholders.append(", ");
            }
            placeholders.append("?");
        }

        String sql = "SELECT ec.event_id, c.club_id, c.club_slug, c.club_name, c.icon_class, c.accent_theme "
                + "FROM event_clubs ec JOIN clubs c ON c.club_id = ec.club_id "
                + "WHERE ec.event_id IN (" + placeholders + ") ORDER BY c.club_id ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            for (Integer id : byId.keySet()) {
                ps.setInt(idx++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Event target = byId.get(rs.getInt("event_id"));
                    if (target != null) {
                        Club c = new Club();
                        c.setClubId(rs.getInt("club_id"));
                        c.setClubSlug(rs.getString("club_slug"));
                        c.setClubName(rs.getString("club_name"));
                        c.setIconClass(rs.getString("icon_class"));
                        c.setAccentTheme(rs.getString("accent_theme"));
                        target.getAssociatedClubs().add(c);
                    }
                }
            }
        }
    }

    private Event mapEvent(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setEventId(rs.getInt("event_id"));
        e.setEventCode(rs.getString("event_code"));
        e.setFlagship(rs.getInt("is_flagship") == 1);
        e.setEventName(rs.getString("event_name"));
        e.setDescription(rs.getString("description"));
        Date d = rs.getDate("event_date");
        if (d != null) {
            e.setEventDate(d.toLocalDate());
        }
        Time st = rs.getTime("start_time");
        if (st != null) {
            e.setStartTime(st.toLocalTime());
        }
        Time et = rs.getTime("end_time");
        if (et != null) {
            e.setEndTime(et.toLocalTime());
        }
        e.setVenue(rs.getString("venue"));
        e.setOrganizer(rs.getString("organizer"));
        Timestamp dl = rs.getTimestamp("registration_deadline");
        if (dl != null) {
            e.setRegistrationDeadline(dl.toLocalDateTime());
        }
        int cap = rs.getInt("capacity");
        if (!rs.wasNull()) {
            e.setCapacity(cap);
        }
        e.setStatus(rs.getString("status"));
        int createdBy = rs.getInt("created_by");
        if (!rs.wasNull()) {
            e.setCreatedBy(createdBy);
        }
        e.setCreatedByName(rs.getString("created_by_name"));
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            e.setCreatedAt(createdTs.toLocalDateTime());
        }
        e.setRegisteredCount(rs.getInt("registered_count"));
        e.setPresentCount(rs.getInt("present_count"));
        e.setUserRegistered(rs.getBoolean("is_user_registered"));
        return e;
    }
}
