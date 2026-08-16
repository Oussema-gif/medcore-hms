package com.medcore.hms.laboratory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelLaboratoryRequest(
        @NotBlank
        @Size(max = 512)
        String cancellationReason
) {
}