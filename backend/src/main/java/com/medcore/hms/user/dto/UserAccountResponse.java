package com.medcore.hms.user.dto;

import com.medcore.hms.user.domain.UserAccountStatus;
import com.medcore.hms.user.domain.UserRole;

import java.time.Instant;

public record UserAccountResponse(
        Long id,
        String username,
        String displayName,
        String email,
        UserRole role,
        UserAccountStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
