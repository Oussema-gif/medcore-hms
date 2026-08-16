package com.medcore.hms.billing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateInvoiceRequest(
        @NotNull
        Long patientId,

        Long appointmentId,

        @NotBlank
        @Size(min = 3, max = 3)
        String currencyCode,

        @FutureOrPresent
        LocalDate dueDate,

        @NotEmpty
        List<@Valid Line> lines
) {
    public record Line(
            @NotNull
            @Positive
            Integer lineNumber,

            @NotBlank
            @Size(max = 64)
            String itemType,

            @NotBlank
            @Size(max = 512)
            String description,

            @NotNull
            @DecimalMin("0.0001")
            BigDecimal quantity,

            @NotNull
            @DecimalMin("0.0")
            BigDecimal unitPrice,

            @DecimalMin("0.0")
            BigDecimal discountAmount,

            @DecimalMin("0.0")
            BigDecimal taxAmount
    ) {
    }
}
