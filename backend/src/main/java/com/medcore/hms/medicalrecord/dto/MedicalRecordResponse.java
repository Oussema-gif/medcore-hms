package com.medcore.hms.medicalrecord.dto;

import java.time.Instant;

public record MedicalRecordResponse(
        Long id,
        Long patientId,
        Long doctorId,
        Long appointmentId,
        Instant encounterAt,
        String symptoms,
        String clinicalNotes,
        boolean patientVisible,
        Instant releasedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
