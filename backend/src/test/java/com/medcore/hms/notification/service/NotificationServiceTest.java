package com.medcore.hms.notification.service;

import com.medcore.hms.notification.domain.Notification;
import com.medcore.hms.notification.dto.NotificationResponse;
import com.medcore.hms.notification.repository.NotificationRepository;
import com.medcore.hms.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private NotificationRepository notificationRepository;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        notificationService = new NotificationService(notificationRepository);
    }

    @Test
    void findForUserUsesAuthenticatedUserId() {
        Long userAccountId = 10L;
        Pageable pageable = PageRequest.of(0, 20);
        Notification notification = mock(Notification.class);

        when(notificationRepository
                .findByUserAccountIdOrderByCreatedAtDesc(
                        userAccountId,
                        pageable))
                .thenReturn(new PageImpl<>(List.of(notification)));

        var result = notificationService.findForUser(
                userAccountId,
                pageable);

        assertThat(result.getContent()).hasSize(1);

        verify(notificationRepository)
                .findByUserAccountIdOrderByCreatedAtDesc(
                        userAccountId,
                        pageable);
    }

    @Test
    void markAsReadScopesLookupToAuthenticatedUserId() {
        Long notificationId = 5L;
        Long userAccountId = 10L;
        Notification notification = mock(Notification.class);

        when(notificationRepository.findByIdAndUserAccountId(
                notificationId,
                userAccountId))
                .thenReturn(Optional.of(notification));

        notificationService.markAsRead(
                notificationId,
                userAccountId);

        verify(notificationRepository)
                .findByIdAndUserAccountId(
                        notificationId,
                        userAccountId);

        verify(notification).markAsRead();
    }

    @Test
    void markAsReadRejectsNotificationOwnedByAnotherUser() {
        Long notificationId = 5L;
        Long userAccountId = 10L;

        when(notificationRepository.findByIdAndUserAccountId(
                notificationId,
                userAccountId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                notificationService.markAsRead(
                        notificationId,
                        userAccountId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(notificationRepository)
                .findByIdAndUserAccountId(
                        notificationId,
                        userAccountId);
    }
}