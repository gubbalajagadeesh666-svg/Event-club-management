package com.campusflow.dao;

import com.campusflow.config.DBConnection;
import com.campusflow.model.Club;
import com.campusflow.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC Data Access Object for clubs and student club memberships.
 */
public class ClubDAO {

    private static final String BASE_SELECT = """
            SELECT c.club_id, c.club_slug, c.club_name, c.focus_area, c.description, c.activities,
                   c.icon_class, c.accent_theme, c.coordinator_id, coord.full_name AS coordinator_name,
                   c.leader_id, ldr.full_name AS leader_name, c.created_at,
                   (SELECT COUNT(*) FROM club_memberships cm
                    WHERE cm.club_id = c.club_id AND cm.status = 'ACTIVE') AS member_count,
                   (SELECT COUNT(*) FROM event_clubs ec
                    JOIN events e ON e.event_id = ec.event_id
                    WHERE ec.club_id = c.club_id AND e.status IN ('APPROVED', 'COMPLETED', 'SCHEDULE_PENDING')) AS event_count,
                   EXISTS(SELECT 1 FROM club_memberships cm2
                          WHERE cm2.club_id = c.club_id AND cm2.user_id = ? AND cm2.status = 'ACTIVE') AS is_user_member
            FROM clubs c
            LEFT JOIN users coord ON coord.user_id = c.coordinator_id
            LEFT JOIN users ldr ON ldr.user_id = c.leader_id
            """;

    public List<Club> findAllClubs(Integer currentUserId) throws SQLException {
        String sql = BASE_SELECT + " ORDER BY c.club_id ASC";
        List<Club> clubs = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUserId == null ? -1 : currentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clubs.add(mapClub(rs));
                }
            }
        }
        return clubs;
    }

    public Club findById(int clubId, Integer currentUserId) throws SQLException {
        String sql = BASE_SELECT + " WHERE c.club_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUserId == null ? -1 : currentUserId);
            ps.setInt(2, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapClub(rs) : null;
            }
        }
    }

    public List<Club> findByCoordinator(int coordinatorUserId) throws SQLException {
        String sql = BASE_SELECT + " WHERE c.coordinator_id = ? ORDER BY c.club_name ASC";
        List<Club> clubs = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, coordinatorUserId);
            ps.setInt(2, coordinatorUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clubs.add(mapClub(rs));
                }
            }
        }
        return clubs;
    }

    public List<Club> findByLeader(int leaderUserId) throws SQLException {
        String sql = BASE_SELECT + " WHERE c.leader_id = ? ORDER BY c.club_name ASC";
        List<Club> clubs = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, leaderUserId);
            ps.setInt(2, leaderUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clubs.add(mapClub(rs));
                }
            }
        }
        return clubs;
    }

    public int createClub(Club club) throws SQLException {
        String sql = """
                INSERT INTO clubs (club_slug, club_name, focus_area, description, activities,
                                   icon_class, accent_theme, coordinator_id, leader_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, club.getClubSlug());
            ps.setString(2, club.getClubName());
            ps.setString(3, club.getFocusArea());
            ps.setString(4, club.getDescription());
            ps.setString(5, club.getActivities());
            ps.setString(6, club.getIconClass());
            ps.setString(7, club.getAccentTheme());
            if (club.getCoordinatorId() != null) {
                ps.setInt(8, club.getCoordinatorId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            if (club.getLeaderId() != null) {
                ps.setInt(9, club.getLeaderId());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public boolean updateClubByAdmin(Club club) throws SQLException {
        String sql = """
                UPDATE clubs
                SET club_name = ?, focus_area = ?, description = ?, activities = ?,
                    icon_class = ?, accent_theme = ?, coordinator_id = ?, leader_id = ?
                WHERE club_id = ?
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, club.getClubName());
            ps.setString(2, club.getFocusArea());
            ps.setString(3, club.getDescription());
            ps.setString(4, club.getActivities());
            ps.setString(5, club.getIconClass());
            ps.setString(6, club.getAccentTheme());
            if (club.getCoordinatorId() != null) {
                ps.setInt(7, club.getCoordinatorId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            if (club.getLeaderId() != null) {
                ps.setInt(8, club.getLeaderId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            ps.setInt(9, club.getClubId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateClubByLeader(int clubId, int leaderUserId, String focusArea,
                                      String description, String activities) throws SQLException {
        String sql = """
                UPDATE clubs
                SET focus_area = ?, description = ?, activities = ?
                WHERE club_id = ? AND leader_id = ?
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, focusArea);
            ps.setString(2, description);
            ps.setString(3, activities);
            ps.setInt(4, clubId);
            ps.setInt(5, leaderUserId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean hasLinkedRecords(int clubId) throws SQLException {
        String sql = """
                SELECT (SELECT COUNT(*) FROM club_memberships WHERE club_id = ?) +
                       (SELECT COUNT(*) FROM event_clubs WHERE club_id = ?)
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clubId);
            ps.setInt(2, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean deleteClub(int clubId) throws SQLException {
        String sql = "DELETE FROM clubs WHERE club_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clubId);
            return ps.executeUpdate() > 0;
        }
    }

    public void joinClub(int clubId, int userId) throws SQLException {
        String sql = """
                INSERT INTO club_memberships (club_id, user_id, status, joined_at)
                VALUES (?, ?, 'ACTIVE', CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE status = 'ACTIVE', joined_at = CURRENT_TIMESTAMP
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clubId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void leaveClub(int clubId, int userId) throws SQLException {
        String sql = "UPDATE club_memberships SET status = 'LEFT' WHERE club_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clubId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public List<User> findActiveMembers(int clubId) throws SQLException {
        String sql = """
                SELECT u.user_id, u.full_name, u.email, u.role, u.status, cm.joined_at
                FROM club_memberships cm
                JOIN users u ON u.user_id = cm.user_id
                WHERE cm.club_id = ? AND cm.status = 'ACTIVE'
                ORDER BY cm.joined_at DESC
                """;
        List<User> members = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = new User();
                    u.setUserId(rs.getInt("user_id"));
                    u.setFullName(rs.getString("full_name"));
                    u.setEmail(rs.getString("email"));
                    u.setRole(rs.getString("role"));
                    u.setStatus(rs.getString("status"));
                    Timestamp ts = rs.getTimestamp("joined_at");
                    if (ts != null) {
                        u.setCreatedAt(ts.toLocalDateTime());
                    }
                    members.add(u);
                }
            }
        }
        return members;
    }

    private Club mapClub(ResultSet rs) throws SQLException {
        Club c = new Club();
        c.setClubId(rs.getInt("club_id"));
        c.setClubSlug(rs.getString("club_slug"));
        c.setClubName(rs.getString("club_name"));
        c.setFocusArea(rs.getString("focus_area"));
        c.setDescription(rs.getString("description"));
        c.setActivities(rs.getString("activities"));
        c.setIconClass(rs.getString("icon_class"));
        c.setAccentTheme(rs.getString("accent_theme"));
        int coordId = rs.getInt("coordinator_id");
        if (!rs.wasNull()) {
            c.setCoordinatorId(coordId);
        }
        c.setCoordinatorName(rs.getString("coordinator_name"));
        int leadId = rs.getInt("leader_id");
        if (!rs.wasNull()) {
            c.setLeaderId(leadId);
        }
        c.setLeaderName(rs.getString("leader_name"));
        c.setMemberCount(rs.getInt("member_count"));
        c.setEventCount(rs.getInt("event_count"));
        c.setUserMember(rs.getBoolean("is_user_member"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            c.setCreatedAt(ts.toLocalDateTime());
        }
        return c;
    }
}