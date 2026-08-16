package com.medcore.hms.billing.dto;

import com.medcore.hms.billing.domain.PaymentMethod;
import com.medcore.hms.billing.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        String paymentNumber,
        Long invoiceId,
        Long patientId,
        BigDecimal amount,
        String currencyCode,
        PaymentMethod paymentMethod,
        String paymentReference,
        Instant paidAt,
        Long recordedByUserId,
        PaymentStatus status,
        Instant voidedAt,
        String voidReason
) {
}
