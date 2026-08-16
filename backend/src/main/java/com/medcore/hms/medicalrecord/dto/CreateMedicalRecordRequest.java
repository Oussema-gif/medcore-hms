package com.medcore.hms.medicalrecord.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateMedicalRecordRequest(
        @NotNull
        Long patientId,

        @NotNull
        Long doctorId,

        Long appointmentId,

        @NotNull
        Instant encounterAt,

        @Size(max = 10000)
        String symptoms,

        @NotBlank
        @Size(max = 10000)
        String clinicalNotes
) {
}
