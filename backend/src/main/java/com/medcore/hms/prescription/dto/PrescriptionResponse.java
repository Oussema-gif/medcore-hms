package com.medcore.hms.prescription.dto;

import com.medcore.hms.prescription.domain.PrescriptionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PrescriptionResponse(
        Long id,
        Long patientId,
        Long doctorId,
        Long appointmentId,
        Long medicalRecordId,
        PrescriptionStatus status,
        Instant prescribedAt,
        Instant expiresAt,
        String notes,
        Instant cancelledAt,
        String cancellationReason,
        List<Item> items
) {
    public record Item(
            Long id,
            Long medicineId,
            String dosage,
            String route,
            String frequency,
            Integer durationDays,
            BigDecimal quantityPrescribed,
            String instructions,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
