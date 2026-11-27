package com.campus.placement.service;

import com.campus.placement.dao.JdbcNotificationDao;
import com.campus.placement.dao.NotificationDao;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.model.Notification;

import java.util.List;

/**
 * Service for fetching and marking user {@link Notification} items as read.
 */
public class NotificationService {

    private final NotificationDao notificationDao;

    public NotificationService() {
        this(new JdbcNotificationDao());
    }

    public NotificationService(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    public List<Notification> getForUser(int userId) throws DataAccessException {
        return notificationDao.findByUserId(userId);
    }

    public void markAsRead(int notificationId) throws DataAccessException {
        notificationDao.markAsRead(notificationId);
    }
}
