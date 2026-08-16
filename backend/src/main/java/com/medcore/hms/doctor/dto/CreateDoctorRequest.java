package com.medcore.hms.doctor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateDoctorRequest(
        @NotNull
        Long userAccountId,

        @NotNull
        Long departmentId,

        @NotBlank
        @Size(max = 64)
        String professionalRegistrationNumber,

        @NotBlank
        @Size(max = 128)
        String firstName,

        @NotBlank
        @Size(max = 128)
        String lastName,

        @Size(max = 256)
        String specialty,

        @Size(max = 32)
        String phoneNumber,

        @Email
        @Size(max = 254)
        String email,

        @NotNull
        @Positive
        Integer consultationDurationMinutes
) {
}
