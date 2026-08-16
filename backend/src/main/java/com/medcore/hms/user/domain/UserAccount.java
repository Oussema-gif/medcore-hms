package com.medcore.hms.user.domain;

import com.medcore.hms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "user_account")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAccount extends BaseEntity {

    @Column(nullable = false, length = 64, unique = true)
    private String username;

    @Column(name = "display_name", length = 160)
    private String displayName;

    @Column(nullable = false, length = 254, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserAccountStatus status = UserAccountStatus.ACTIVE;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by_user_id")
    private Long deletedByUserId;

    public boolean canAuthenticate(Instant now) {
        return status == UserAccountStatus.ACTIVE
                && deletedAt == null
                && (lockedUntil == null || !lockedUntil.isAfter(now));
    }

    public void recordSuccessfulLogin(Instant occurredAt) {
        lastLoginAt = occurredAt;
        failedLoginCount = 0;
        lockedUntil = null;
        status = UserAccountStatus.ACTIVE;
    }

    public void recordFailedLogin() {
        failedLoginCount++;
    }

    public void lockUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
        this.status = UserAccountStatus.LOCKED;
    }

    public void disable() {
        status = UserAccountStatus.DISABLED;
    }

    public void enable() {
        status = UserAccountStatus.ACTIVE;
        lockedUntil = null;
        failedLoginCount = 0;
    }

    public void markPasswordChanged(Instant changedAt) {
        passwordChangedAt = changedAt;
        failedLoginCount = 0;
        lockedUntil = null;
        status = UserAccountStatus.ACTIVE;
    }

    public void softDelete(Long actorUserId, Instant deletedAt) {
        this.deletedByUserId = actorUserId;
        this.deletedAt = deletedAt;
        this.status = UserAccountStatus.DISABLED;
    }

    public Set<String> getRoleNames() {
        return role == null ? Set.of() : Set.of(role.name());
    }

    public static UserAccount create() {
        return new UserAccount();
    }
}