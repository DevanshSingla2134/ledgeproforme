package com.ledgepro.model;

import java.time.LocalDateTime;

/**
 * Base abstract entity demonstrating OOP Inheritance,
 * Encapsulation, and common lifecycle fields.
 */
public abstract class BaseEntity {
    protected Long id;
    protected LocalDateTime createdAt;

    public BaseEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public BaseEntity(Long id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
