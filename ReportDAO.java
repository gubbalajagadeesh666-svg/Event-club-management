package com.campusflow.dao;

import com.campusflow.config.DBConnection;
import com.campusflow.model.ClubReportRow;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC Data Access Object for real SQL-aggregated dashboard and report statistics.
 * Never returns fabricated metrics; all counts reflect actual MySQL records.
 */
public class ReportDAO {

    public record SystemSummary(
            int totalClubs,
            int totalEvents,
            int upcomingEvents,
            int pendingApprovals,
            int activeUsers,
            int totalRegistrations,
            int totalPresentAttendance
    ) {
    }

    public SystemSummary getSystemSummary() throws SQLException {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM clubs) AS total_clubs,
                    (SELECT COUNT(*) FROM events) AS total_events,
                    (SELECT COUNT(*) FROM events
                     WHERE status = 'APPROVED' AND (event_date IS NULL OR event_date >= CURDATE())) AS upcoming_events,
                    (SELECT COUNT(*) FROM events WHERE status = 'PENDING_APPROVAL') AS pending_approvals,
                    (SELECT COUNT(*) FROM users WHERE status = 'ACTIVE') AS active_users,
                    (SELECT COUNT(*) FROM event_registrations WHERE status = 'REGISTERED') AS total_registrations,
                    (SELECT COUNT(*) FROM attendance WHERE attendance_status = 'PRESENT') AS total_present
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new SystemSummary(
                        rs.getInt("total_clubs"),
                        rs.getInt("total_events"),
                        rs.getInt("upcoming_events"),
                        rs.getInt("pending_approvals"),
                        rs.getInt("active_users"),
                        rs.getInt("total_registrations"),
                        rs.getInt("total_present")
                );
            }
            return new SystemSummary(0, 0, 0, 0, 0, 0, 0);
        }
    }

    public List<ClubReportRow> getParticipationByClub() throws SQLException {
        String sql = """
                SELECT
                    c.club_id,
                    c.club_name,
                    c.icon_class,
                    c.accent_theme,
                    (SELECT COUNT(*) FROM club_memberships cm
                     WHERE cm.club_id = c.club_id AND cm.status = 'ACTIVE') AS active_members,
                    (SELECT COUNT(*) FROM event_clubs ec
                     WHERE ec.club_id = c.club_id) AS total_events,
                    (SELECT COUNT(*) FROM event_clubs ec2
                     JOIN events e2 ON e2.event_id = ec2.event_id
                     WHERE ec2.club_id = c.club_id AND e2.status IN ('APPROVED', 'COMPLETED')) AS approved_events,
                    (SELECT COUNT(er.registration_id)
                     FROM event_clubs ec3
                     JOIN event_registrations er ON er.event_id = ec3.event_id AND er.status = 'REGISTERED'
                     WHERE ec3.club_id = c.club_id) AS total_registrations,
                    (SELECT COUNT(a.attendance_id)
                     FROM event_clubs ec4
                     JOIN attendance a ON a.event_id = ec4.event_id AND a.attendance_status = 'PRESENT'
                     WHERE ec4.club_id = c.club_id) AS present_attendance
                FROM clubs c
                ORDER BY c.club_id ASC
                """;
        List<ClubReportRow> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ClubReportRow row = new ClubReportRow();
                row.setClubId(rs.getInt("club_id"));
                row.setClubName(rs.getString("club_name"));
                row.setIconClass(rs.getString("icon_class"));
                row.setAccentTheme(rs.getString("accent_theme"));
                row.setActiveMembers(rs.getInt("active_members"));
                row.setTotalEvents(rs.getInt("total_events"));
                row.setApprovedEvents(rs.getInt("approved_events"));
                row.setTotalRegistrations(rs.getInt("total_registrations"));
                row.setPresentAttendance(rs.getInt("present_attendance"));
                rows.add(row);
            }
        }
        return rows;
    }
}
