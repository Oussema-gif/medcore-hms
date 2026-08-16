package com.medcore.hms.appointment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateAppointmentRequest(
        @Future
        Instant scheduledStartAt,

        @Future
        Instant scheduledEndAt,

        @Size(max = 512)
        String reason,

        @Size(max = 1024)
        String schedulingNotes
) {
}
