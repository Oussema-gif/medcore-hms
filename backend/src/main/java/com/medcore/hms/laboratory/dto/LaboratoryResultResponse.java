package com.medcore.hms.laboratory.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record LaboratoryResultResponse(
        Long id,
        Long laboratoryRequestItemId,
        BigDecimal resultValue,
        String resultText,
        String unit,
        String referenceRange,
        String interpretation,
        Long recordedByUserId,
        Instant recordedAt,
        Instant releasedAt,
        Long amendedFromResultId,
        Instant createdAt,
        Instant updatedAt
) {
}
