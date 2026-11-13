package com.campusflow.dao;

import com.campusflow.config.DBConnection;
import com.campusflow.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC Data Access Object for user accounts, authentication lookups, and role administration.
 */
public class UserDAO {

    public int countAdmins() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countActiveAdmins() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND status = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Controlled initial setup: creates the first Administrator account inside a transaction
     * only if zero Administrator accounts exist in the database.
     */
    public User createInitialAdminIfNoneExists(String fullName, String normalizedEmail, String passwordHash)
            throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement checkPs = conn.prepareStatement(
                        "SELECT COUNT(*) FROM users WHERE role = 'ADMIN' FOR UPDATE");
                     ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        conn.rollback();
                        return null;
                    }
                }

                String insertSql = "INSERT INTO users (full_name, email, password_hash, role, status) "
                        + "VALUES (?, ?, ?, 'ADMIN', 'ACTIVE')";
                int generatedId;
                try (PreparedStatement insPs = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    insPs.setString(1, fullName);
                    insPs.setString(2, normalizedEmail);
                    insPs.setString(3, passwordHash);
                    insPs.executeUpdate();
                    try (ResultSet keys = insPs.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Failed to retrieve generated user_id for initial admin.");
                        }
                        generatedId = keys.getInt(1);
                    }
                }

                conn.commit();
                User admin = new User();
                admin.setUserId(generatedId);
                admin.setFullName(fullName);
                admin.setEmail(normalizedEmail);
                admin.setRole(User.ROLE_ADMIN);
                admin.setStatus(User.STATUS_ACTIVE);
                return admin;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean emailExists(String normalizedEmail) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedEmail);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public User createUser(String fullName, String normalizedEmail, String passwordHash, String role, String status)
            throws SQLException {
        String sql = "INSERT INTO users (full_name, email, password_hash, role, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fullName);
            ps.setString(2, normalizedEmail);
            ps.setString(3, passwordHash);
            ps.setString(4, role);
            ps.setString(5, status);
            ps.executeUpdate();

            User user = new User();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setUserId(keys.getInt(1));
                }
            }
            user.setFullName(fullName);
            user.setEmail(normalizedEmail);
            user.setRole(role);
            user.setStatus(status);
            return user;
        }
    }

    public User findByEmailForAuth(String normalizedEmail) throws SQLException {
        String sql = "SELECT user_id, full_name, email, password_hash, role, status, created_at "
                + "FROM users WHERE email = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedEmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                User u = mapPublicColumns(rs);
                u.setPasswordHash(rs.getString("password_hash"));
                return u;
            }
        }
    }

    public User findById(int userId) throws SQLException {
        String sql = "SELECT user_id, full_name, email, role, status, created_at FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapPublicColumns(rs) : null;
            }
        }
    }

    public List<User> findAllUsers() throws SQLException {
        String sql = "SELECT user_id, full_name, email, role, status, created_at "
                + "FROM users ORDER BY FIELD(role, 'ADMIN', 'FACULTY_COORDINATOR', 'CLUB_LEADER', 'STUDENT'), full_name ASC";
        List<User> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapPublicColumns(rs));
            }
        }
        return list;
    }

    public List<User> findActiveUsersByRoles(String... roles) throws SQLException {
        if (roles == null || roles.length == 0) {
            return new ArrayList<>();
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < roles.length; i++) {
            if (i > 0) {
                placeholders.append(", ");
            }
            placeholders.append("?");
        }
        String sql = "SELECT user_id, full_name, email, role, status, created_at "
                + "FROM users WHERE status = 'ACTIVE' AND role IN (" + placeholders + ") ORDER BY full_name ASC";
        List<User> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < roles.length; i++) {
                ps.setString(i + 1, roles[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPublicColumns(rs));
                }
            }
        }
        return list;
    }

    public boolean updateRoleAndStatus(int userId, String role, String status) throws SQLException {
        String sql = "UPDATE users SET role = ?, status = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setString(2, status);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        }
    }

    private User mapPublicColumns(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setRole(rs.getString("role"));
        u.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            u.setCreatedAt(ts.toLocalDateTime());
        }
        return u;
    }
}
