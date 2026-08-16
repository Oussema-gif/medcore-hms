package com.medcore.hms.medicalrecord.dto;

import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateMedicalRecordRequest(
        Instant encounterAt,

        @Size(max = 10000)
        String symptoms,

        @Size(max = 10000)
        String clinicalNotes
) {
}
