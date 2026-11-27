package com.campus.placement.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Audit trail entry for an application's stage transitions.
 */
public class ApplicationStatusHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int applicationId;
    private ApplicationStatus fromStatus;
    private ApplicationStatus toStatus;
    private Integer changedByUserId;
    private String remarks;
    private LocalDateTime changedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public ApplicationStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(ApplicationStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public ApplicationStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(ApplicationStatus toStatus) {
        this.toStatus = toStatus;
    }

    public Integer getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(Integer changedByUserId) {
        this.changedByUserId = changedByUserId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    @Override
    public String toString() {
        String from = fromStatus == null ? "START" : fromStatus.name();
        return from + " -> " + toStatus + (remarks != null && !remarks.isBlank() ? " (" + remarks + ")" : "");
    }
}
