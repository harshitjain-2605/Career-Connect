package com.campus.placement.dao;

import com.campus.placement.exception.DataAccessException;
import com.campus.placement.model.Notification;

import java.sql.Connection;
import java.util.List;

/**
 * DAO contract for {@link Notification} entities.
 */
public interface NotificationDao extends Dao<Notification, Integer> {

    List<Notification> findByUserId(int userId) throws DataAccessException;

    void saveInTransaction(Connection conn, Notification notification) throws DataAccessException;

    boolean markAsRead(int notificationId) throws DataAccessException;
}
