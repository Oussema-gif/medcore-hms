package com.medcore.hms.laboratory.dto;

import com.medcore.hms.laboratory.domain.LaboratoryPriority;
import com.medcore.hms.laboratory.domain.LaboratoryRequestStatus;

import java.time.Instant;
import java.util.List;

public record LaboratoryRequestResponse(
        Long id,
        Long patientId,
        Long orderingDoctorId,
        Long appointmentId,
        Long medicalRecordId,
        LaboratoryPriority priority,
        LaboratoryRequestStatus status,
        String clinicalNotes,
        Instant requestedAt,
        Instant releasedAt,
        Instant cancelledAt,
        String cancellationReason,
        List<LaboratoryTestItemResponse> items
) {
    public record LaboratoryTestItemResponse(
            Long id,
            String testCode,
            String testName,
            Integer sortOrder
    ) {
    }
}