package com.campus.placement.model;

/**
 * In-app notification sent to a user (e.g., offer issued, shortlist update).
 */
public class Notification extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private int userId;
    private String title;
    private String message;
    private boolean read;

    public Notification() {
    }

    public Notification(int userId, String title, String message) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.read = false;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    @Override
    public String toString() {
        return (read ? "" : "[NEW] ") + title + ": " + message;
    }
}
