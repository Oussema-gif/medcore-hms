package com.medcore.hms.billing.dto;

import com.medcore.hms.billing.domain.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotNull
        Long invoiceId,

        @NotNull
        @DecimalMin("0.0001")
        BigDecimal amount,

        @NotNull
        PaymentMethod paymentMethod,

        @Size(max = 256)
        String paymentReference
) {
}
