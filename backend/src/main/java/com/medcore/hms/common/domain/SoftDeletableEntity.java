package com.medcore.hms.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
public abstract class SoftDeletableEntity extends BaseEntity {

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by_user_id")
    private Long deletedByUserId;

    public void softDelete(Long actorUserId, Instant deletedAt) {
        this.active = false;
        this.deletedAt = deletedAt;
        this.deletedByUserId = actorUserId;
    }

    public void restore() {
        this.active = true;
        this.deletedAt = null;
        this.deletedByUserId = null;
    }

    public boolean isDeleted() {
        return deletedAt != null || !active;
    }
}