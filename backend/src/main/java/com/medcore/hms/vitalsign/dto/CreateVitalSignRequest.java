package com.medcore.hms.vitalsign.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateVitalSignRequest(
        @NotNull
        Long medicalRecordId,

        @NotNull
        Instant recordedAt,

        @DecimalMin("25.00")
        @DecimalMax("50.00")
        BigDecimal temperatureCelsius,

        @Min(40)
        @Max(300)
        Short systolicBp,

        @Min(20)
        @Max(200)
        Short diastolicBp,

        @Min(20)
        @Max(300)
        Short heartRate,

        @Min(4)
        @Max(80)
        Short respiratoryRate,

        @DecimalMin("0.00")
        @DecimalMax("100.00")
        BigDecimal oxygenSaturation,

        @DecimalMin(value = "0.01")
        BigDecimal weightKg,

        @DecimalMin(value = "0.01")
        BigDecimal heightCm
) {
}
