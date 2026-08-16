package com.medcore.hms.laboratory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateLaboratoryResultRequest(
        @Positive
        Long laboratoryRequestItemId,

        @DecimalMin("0.0")
        BigDecimal resultValue,

        String resultText,

        String unit,

        String referenceRange,

        String interpretation,

        Long amendedFromResultId
) {
}
