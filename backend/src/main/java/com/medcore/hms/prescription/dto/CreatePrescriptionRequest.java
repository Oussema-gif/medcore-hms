package com.medcore.hms.prescription.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CreatePrescriptionRequest(
        @NotNull
        Long patientId,

        @NotNull
        Long doctorId,

        Long appointmentId,

        Long medicalRecordId,

        @Future
        Instant expiresAt,

        @Size(max = 2048)
        String notes,

        @NotEmpty
        List<@Valid Item> items
) {
    public record Item(
            @NotNull
            Long medicineId,

            @NotBlank
            @Size(max = 128)
            String dosage,

            @NotBlank
            @Size(max = 64)
            String route,

            @NotBlank
            @Size(max = 128)
            String frequency,

            @NotNull
            @Positive
            Integer durationDays,

            @NotNull
            @DecimalMin("0.0001")
            BigDecimal quantityPrescribed,

            @Size(max = 2048)
            String instructions
    ) {
    }
}
