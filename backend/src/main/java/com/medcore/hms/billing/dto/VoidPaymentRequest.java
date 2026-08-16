package com.medcore.hms.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VoidPaymentRequest(
        @NotBlank
        @Size(max = 512)
        String reason
) {
}
