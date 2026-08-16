package com.medcore.hms.notification.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_account_id", nullable = false)
    private Long userAccountId;

    @Column(name = "notification_type", nullable = false, length = 64)
    private String notificationType;

    @Column(nullable = false, length = 256)
    private String title;

    @Column(nullable = false, length = 2048)
    private String body;

    @Column(name = "resource_type", length = 64)
    private String resourceType;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public static Notification create(
            Long userAccountId,
            String notificationType,
            String title,
            String body) {

        Notification notification = new Notification();
        notification.userAccountId = userAccountId;
        notification.notificationType = notificationType;
        notification.title = title;
        notification.body = body;
        return notification;
    }

    public boolean isRead() {
        return readAt != null;
    }

    public void markAsRead() {
        if (readAt == null) {
            readAt = Instant.now();
        }
    }
}
