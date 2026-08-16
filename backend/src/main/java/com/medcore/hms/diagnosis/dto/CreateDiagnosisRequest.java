package com.medcore.hms.diagnosis.dto;

import com.medcore.hms.diagnosis.domain.DiagnosisType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDiagnosisRequest(
        @NotNull
        Long medicalRecordId,

        @Size(max = 64)
        String codeSystem,

        @Size(max = 64)
        String diagnosisCode,

        @NotBlank
        @Size(max = 512)
        String description,

        @NotNull
        DiagnosisType diagnosisType,

        boolean primary
) {
}
