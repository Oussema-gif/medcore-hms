package com.medcore.hms.notification.dto;

import com.medcore.hms.notification.domain.Notification;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        String notificationType,
        String title,
        String body,
        String resourceType,
        Long resourceId,
        Instant readAt,
        Instant createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getResourceType(),
                notification.getResourceId(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}
