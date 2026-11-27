package com.campus.placement.model;

/**
 * Immutable-style audit trail entry recording who changed what and when.
 */
public class AuditLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Integer actorUserId;
    private String action;
    private String entityType;
    private Integer entityId;
    private String details;

    public AuditLog() {
    }

    public AuditLog(Integer actorUserId, String action, String entityType, Integer entityId, String details) {
        this.actorUserId = actorUserId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
    }

    public Integer getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(Integer actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
