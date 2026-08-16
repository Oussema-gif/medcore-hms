package com.medcore.hms.doctor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateDoctorRequest(
        Long userAccountId,

        Long departmentId,

        @Size(max = 64)
        String professionalRegistrationNumber,

        @Size(max = 128)
        String firstName,

        @Size(max = 128)
        String lastName,

        @Size(max = 256)
        String specialty,

        @Size(max = 32)
        String phoneNumber,

        @Email
        @Size(max = 254)
        String email,

        @Positive
        Integer consultationDurationMinutes
) {
}
