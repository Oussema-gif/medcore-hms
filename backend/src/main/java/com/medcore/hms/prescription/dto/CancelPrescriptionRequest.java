package com.medcore.hms.prescription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelPrescriptionRequest(
        @NotBlank
        @Size(max = 512)
        String cancellationReason
) {
}
