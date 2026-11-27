package com.campus.placement.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Abstract base entity providing common {@code id} and {@code createdAt} fields
 * for all persistent domain models.
 */
// [Unit 1: Abstract classes] & [Unit 3: Object Serialization]
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    protected int id;
    protected LocalDateTime createdAt;

    /**
     * Default constructor.
     */
    protected BaseEntity() {
    }

    /**
     * Constructs an entity with an existing database identifier.
     *
     * @param id primary key
     */
    protected BaseEntity(int id) {
        this.id = id;
    }

    /**
     * Returns the primary key ID.
     *
     * @return entity ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the primary key ID.
     *
     * @param id entity ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the creation timestamp.
     *
     * @return creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
