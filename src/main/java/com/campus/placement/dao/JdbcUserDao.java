package com.campus.placement.dao;

import com.campus.placement.db.ConnectionManager;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.model.Role;
import com.campus.placement.model.User;
import com.campus.placement.util.DateUtil;
import com.campus.placement.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of {@link UserDao} using {@link PreparedStatement}.
 */
// [Unit 4: JDBC CRUD, PreparedStatement, ResultSet, DAO implementation]
public class JdbcUserDao implements UserDao {

    private static final Logger LOGGER = LoggerUtil.getLogger(JdbcUserDao.class);

    @Override
    public User save(User user) throws DataAccessException {
        String sql = "INSERT INTO users (email, password_hash, role, company_id, active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            if (user.getCompanyId() != null) {
                ps.setInt(4, user.getCompanyId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setInt(5, user.isActive() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
            return user;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save user: " + user.getEmail(), e);
            throw new DataAccessException("Failed to save user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(Integer id) throws DataAccessException {
        String sql = "SELECT id, email, password_hash, role, company_id, active, created_at FROM users WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find user by id=" + id, e);
            throw new DataAccessException("Failed to find user by id", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) throws DataAccessException {
        String sql = "SELECT id, email, password_hash, role, company_id, active, created_at FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find user by email=" + email, e);
            throw new DataAccessException("Failed to find user by email", e);
        }
    }

    @Override
    public List<User> findAll() throws DataAccessException {
        String sql = "SELECT id, email, password_hash, role, company_id, active, created_at FROM users ORDER BY id";
        List<User> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to list users", e);
            throw new DataAccessException("Failed to list users", e);
        }
    }

    @Override
    public boolean update(User user) throws DataAccessException {
        String sql = "UPDATE users SET email = ?, password_hash = ?, role = ?, company_id = ?, active = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            if (user.getCompanyId() != null) {
                ps.setInt(4, user.getCompanyId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setInt(5, user.isActive() ? 1 : 0);
            ps.setInt(6, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update user id=" + user.getId(), e);
            throw new DataAccessException("Failed to update user", e);
        }
    }

    @Override
    public boolean setActive(int userId, boolean active) throws DataAccessException {
        String sql = "UPDATE users SET active = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to set active=" + active + " for userId=" + userId, e);
            throw new DataAccessException("Failed to update user active status", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DataAccessException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete user id=" + id, e);
            throw new DataAccessException("Failed to delete user", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(Role.valueOf(rs.getString("role")));
        int compId = rs.getInt("company_id");
        u.setCompanyId(rs.wasNull() ? null : compId);
        u.setActive(rs.getInt("active") == 1);
        u.setCreatedAt(DateUtil.parseDateTime(rs.getString("created_at")));
        return u;
    }
}
