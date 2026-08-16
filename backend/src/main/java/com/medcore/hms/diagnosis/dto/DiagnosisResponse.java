package com.medcore.hms.diagnosis.dto;

import com.medcore.hms.diagnosis.domain.DiagnosisType;

import java.time.Instant;

public record DiagnosisResponse(
        Long id,
        Long medicalRecordId,
        String codeSystem,
        String diagnosisCode,
        String description,
        DiagnosisType diagnosisType,
        boolean primary,
        Instant createdAt,
        Instant updatedAt
) {
}
