package com.campus.placement.dao;

import com.campus.placement.db.ConnectionManager;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.model.AuditLog;
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
 * JDBC implementation of {@link AuditLogDao}.
 */
public class JdbcAuditLogDao implements AuditLogDao {

    private static final Logger LOGGER = LoggerUtil.getLogger(JdbcAuditLogDao.class);

    @Override
    public AuditLog save(AuditLog log) throws DataAccessException {
        try (Connection conn = ConnectionManager.getConnection()) {
            saveInTransaction(conn, log);
            return log;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save audit log", e);
            throw new DataAccessException("Failed to save audit log", e);
        }
    }

    @Override
    public void saveInTransaction(Connection conn, AuditLog log) throws DataAccessException {
        String sql = "INSERT INTO audit_log (actor_user_id, action, entity_type, entity_id, details) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getActorUserId() != null) {
                ps.setInt(1, log.getActorUserId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, log.getAction());
            ps.setString(3, log.getEntityType());
            if (log.getEntityId() != null) {
                ps.setInt(4, log.getEntityId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, log.getDetails());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    log.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert audit log", e);
        }
    }

    @Override
    public Optional<AuditLog> findById(Integer id) throws DataAccessException {
        String sql = "SELECT id, actor_user_id, action, entity_type, entity_id, details, created_at FROM audit_log WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find audit log", e);
        }
    }

    @Override
    public List<AuditLog> findAll() throws DataAccessException {
        return findRecent(200);
    }

    @Override
    public List<AuditLog> findRecent(int limit) throws DataAccessException {
        String sql = "SELECT id, actor_user_id, action, entity_type, entity_id, details, created_at "
                + "FROM audit_log ORDER BY id DESC LIMIT ?";
        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to query audit log", e);
            throw new DataAccessException("Failed to query audit log", e);
        }
    }

    @Override
    public boolean update(AuditLog entity) {
        return false; // Audit logs are append-only
    }

    @Override
    public boolean deleteById(Integer id) throws DataAccessException {
        String sql = "DELETE FROM audit_log WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete audit log", e);
        }
    }

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getInt("id"));
        int actor = rs.getInt("actor_user_id");
        log.setActorUserId(rs.wasNull() ? null : actor);
        log.setAction(rs.getString("action"));
        log.setEntityType(rs.getString("entity_type"));
        int entId = rs.getInt("entity_id");
        log.setEntityId(rs.wasNull() ? null : entId);
        log.setDetails(rs.getString("details"));
        log.setCreatedAt(DateUtil.parseDateTime(rs.getString("created_at")));
        return log;
    }
}
