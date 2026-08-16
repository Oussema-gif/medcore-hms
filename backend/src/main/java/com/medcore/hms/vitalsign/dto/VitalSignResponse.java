package com.medcore.hms.vitalsign.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record VitalSignResponse(
        Long id,
        Long medicalRecordId,
        Instant recordedAt,
        BigDecimal temperatureCelsius,
        Short systolicBp,
        Short diastolicBp,
        Short heartRate,
        Short respiratoryRate,
        BigDecimal oxygenSaturation,
        BigDecimal weightKg,
        BigDecimal heightCm,
        Instant createdAt,
        Instant updatedAt
) {
}
