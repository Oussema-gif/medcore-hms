package com.medcore.hms.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelAppointmentRequest(
        @NotBlank
        @Size(max = 512)
        String cancellationReason
) {
}