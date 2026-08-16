package com.medcore.hms.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AdjustInventoryRequest(
        @NotNull
        BigDecimal quantityDelta,

        @NotBlank
        @Size(max = 512)
        String reason
) {
}
