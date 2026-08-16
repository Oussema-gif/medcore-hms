package com.medcore.hms.notification.service;

import com.medcore.hms.exception.ResourceNotFoundException;
import com.medcore.hms.notification.domain.Notification;
import com.medcore.hms.notification.dto.NotificationResponse;
import com.medcore.hms.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> findForUser(
            Long userAccountId,
            Pageable pageable) {

        return notificationRepository
                .findByUserAccountIdOrderByCreatedAtDesc(
                        userAccountId,
                        pageable)
                .map(NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userAccountId) {
        return notificationRepository
                .countByUserAccountIdAndReadAtIsNull(userAccountId);
    }

    public NotificationResponse markAsRead(
            Long notificationId,
            Long userAccountId) {

        Notification notification = notificationRepository
                .findByIdAndUserAccountId(notificationId, userAccountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification", notificationId));

        notification.markAsRead();

        return NotificationResponse.from(notification);
    }

    public NotificationResponse create(
            Long userAccountId,
            String notificationType,
            String title,
            String body,
            String resourceType,
            Long resourceId) {

        Notification notification = Notification.create(
                userAccountId,
                notificationType,
                title,
                body);

        notification.setResourceType(resourceType);
        notification.setResourceId(resourceId);

        return NotificationResponse.from(
                notificationRepository.save(notification));
    }
}