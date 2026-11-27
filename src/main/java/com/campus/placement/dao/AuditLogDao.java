package com.campus.placement.dao;

import com.campus.placement.exception.DataAccessException;
import com.campus.placement.model.AuditLog;

import java.sql.Connection;
import java.util.List;

/**
 * DAO contract for {@link AuditLog} entries.
 */
public interface AuditLogDao extends Dao<AuditLog, Integer> {

    List<AuditLog> findRecent(int limit) throws DataAccessException;

    void saveInTransaction(Connection conn, AuditLog log) throws DataAccessException;
}
