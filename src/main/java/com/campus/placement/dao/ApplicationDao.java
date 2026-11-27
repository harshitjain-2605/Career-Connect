package com.campus.placement.dao;

import com.campus.placement.exception.DataAccessException;
import com.campus.placement.model.Application;
import com.campus.placement.model.ApplicationStatus;
import com.campus.placement.model.ApplicationStatusHistory;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * DAO contract for {@link Application} and {@link ApplicationStatusHistory}.
 */
public interface ApplicationDao extends Dao<Application, Integer> {

    Optional<Application> findByStudentAndDrive(int studentId, int driveId) throws DataAccessException;

    List<Application> findByDriveId(int driveId) throws DataAccessException;

    List<Application> findByStudentId(int studentId) throws DataAccessException;

    List<ApplicationStatusHistory> findHistoryByApplicationId(int applicationId) throws DataAccessException;

    /**
     * Updates application status and inserts a history row using an existing transaction connection.
     */
    void updateStatusInTransaction(Connection conn, int applicationId, ApplicationStatus fromStatus,
                                   ApplicationStatus toStatus, Integer changedByUserId, String remarks)
            throws DataAccessException;
}
