package com.medcore.hms.appointment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateAppointmentRequest(
        @NotNull
        Long patientId,

        @NotNull
        Long doctorId,

        @NotNull
        Long departmentId,

        @NotNull
        @Future
        Instant scheduledStartAt,

        @NotNull
        @Future
        Instant scheduledEndAt,

        @NotBlank
        @Size(max = 512)
        String reason,

        @Size(max = 1024)
        String schedulingNotes
) {
}
