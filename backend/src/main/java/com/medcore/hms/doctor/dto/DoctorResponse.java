package com.medcore.hms.doctor.dto;

import java.time.Instant;

public record DoctorResponse(
        Long id,
        Long userAccountId,
        Long departmentId,
        String professionalRegistrationNumber,
        String firstName,
        String lastName,
        String specialty,
        String phoneNumber,
        String email,
        Integer consultationDurationMinutes,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
