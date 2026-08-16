package com.medcore.hms.laboratory.dto;

import com.medcore.hms.laboratory.domain.LaboratoryPriority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateLaboratoryRequest(
        @NotNull Long patientId,
        @NotNull Long orderingDoctorId,
        Long appointmentId,
        Long medicalRecordId,
        LaboratoryPriority priority,
        String clinicalNotes,
        @NotEmpty List<@Valid LaboratoryTestItemRequest> items
) {
    public record LaboratoryTestItemRequest(
            @NotBlank String testCode,
            @NotBlank String testName,
            Integer sortOrder
    ) {
    }
}
