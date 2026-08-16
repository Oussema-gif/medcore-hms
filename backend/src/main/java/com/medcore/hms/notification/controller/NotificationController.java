package com.medcore.hms.notification.controller;

import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.common.api.ApiResponse;
import com.medcore.hms.common.api.PagedApiResponse;
import com.medcore.hms.notification.dto.NotificationResponse;
import com.medcore.hms.notification.service.NotificationService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public PagedApiResponse<NotificationResponse> findMine(
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal principal,
            @PageableDefault(size = 20)
            Pageable pageable) {

        return PagedApiResponse.from(
                notificationService.findForUser(
                        principal.getUserAccount().getId(),
                        pageable),
                "Notifications retrieved successfully");
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount(
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal principal) {

        return ApiResponse.success(
                notificationService.countUnread(
                        principal.getUserAccount().getId()),
                "Unread notification count retrieved successfully");
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<NotificationResponse> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal principal) {

        return ApiResponse.success(
                notificationService.markAsRead(
                        notificationId,
                        principal.getUserAccount().getId()),
                "Notification marked as read");
    }
}