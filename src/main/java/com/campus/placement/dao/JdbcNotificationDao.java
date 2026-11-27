package com.campus.placement.dao;

import com.campus.placement.db.ConnectionManager;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.model.Notification;
import com.campus.placement.util.DateUtil;
import com.campus.placement.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of {@link NotificationDao}.
 */
public class JdbcNotificationDao implements NotificationDao {

    private static final Logger LOGGER = LoggerUtil.getLogger(JdbcNotificationDao.class);

    @Override
    public Notification save(Notification n) throws DataAccessException {
        try (Connection conn = ConnectionManager.getConnection()) {
            saveInTransaction(conn, n);
            return n;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save notification", e);
            throw new DataAccessException("Failed to save notification", e);
        }
    }

    @Override
    public void saveInTransaction(Connection conn, Notification n) throws DataAccessException {
        String sql = "INSERT INTO notifications (user_id, title, message, is_read) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, n.getUserId());
            ps.setString(2, n.getTitle());
            ps.setString(3, n.getMessage());
            ps.setInt(4, n.isRead() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    n.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert notification", e);
        }
    }

    @Override
    public Optional<Notification> findById(Integer id) throws DataAccessException {
        String sql = "SELECT id, user_id, title, message, is_read, created_at FROM notifications WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find notification", e);
        }
    }

    @Override
    public List<Notification> findByUserId(int userId) throws DataAccessException {
        String sql = "SELECT id, user_id, title, message, is_read, created_at FROM notifications "
                + "WHERE user_id = ? ORDER BY created_at DESC, id DESC";
        List<Notification> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load notifications for userId=" + userId, e);
            throw new DataAccessException("Failed to load notifications", e);
        }
    }

    @Override
    public List<Notification> findAll() throws DataAccessException {
        String sql = "SELECT id, user_id, title, message, is_read, created_at FROM notifications ORDER BY id DESC";
        List<Notification> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list notifications", e);
        }
    }

    @Override
    public boolean markAsRead(int notificationId) throws DataAccessException {
        String sql = "UPDATE notifications SET is_read = 1 WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to mark notification as read", e);
        }
    }

    @Override
    public boolean update(Notification n) throws DataAccessException {
        String sql = "UPDATE notifications SET title = ?, message = ?, is_read = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, n.getTitle());
            ps.setString(2, n.getMessage());
            ps.setInt(3, n.isRead() ? 1 : 0);
            ps.setInt(4, n.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update notification", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DataAccessException {
        String sql = "DELETE FROM notifications WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete notification", e);
        }
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUserId(rs.getInt("user_id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getInt("is_read") == 1);
        n.setCreatedAt(DateUtil.parseDateTime(rs.getString("created_at")));
        return n;
    }
}
