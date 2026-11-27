package com.campus.placement.service;

import com.campus.placement.dao.AuditLogDao;
import com.campus.placement.dao.JdbcAuditLogDao;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.model.AuditLog;

import java.sql.Connection;
import java.util.List;

/**
 * Service responsible for recording all state-changing actions in {@code audit_log}.
 */
public class AuditService {

    private final AuditLogDao auditLogDao;

    public AuditService() {
        this(new JdbcAuditLogDao());
    }

    public AuditService(AuditLogDao auditLogDao) {
        this.auditLogDao = auditLogDao;
    }

    /**
     * Records an audit log entry.
     */
    public void log(Integer actorUserId, String action, String entityType, Integer entityId, String details)
            throws DataAccessException {
        auditLogDao.save(new AuditLog(actorUserId, action, entityType, entityId, details));
    }

    /**
     * Records an audit log entry inside an existing JDBC transaction.
     */
    public void logInTransaction(Connection conn, Integer actorUserId, String action,
                                 String entityType, Integer entityId, String details)
            throws DataAccessException {
        auditLogDao.saveInTransaction(conn, new AuditLog(actorUserId, action, entityType, entityId, details));
    }

    /**
     * Returns recent audit log entries.
     */
    public List<AuditLog> getRecentLogs(int limit) throws DataAccessException {
        return auditLogDao.findRecent(limit);
    }
}
