package com.medcore.hms.billing.dto;

import com.medcore.hms.billing.domain.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record InvoiceResponse(
        Long id,
        String invoiceNumber,
        Long patientId,
        Long appointmentId,
        InvoiceStatus status,
        String currencyCode,
        LocalDate issueDate,
        LocalDate dueDate,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal balanceAmount,
        Instant issuedAt,
        Instant voidedAt,
        String voidReason,
        List<Line> lines
) {
    public record Line(
            Long id,
            Integer lineNumber,
            String itemType,
            String description,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal discountAmount,
            BigDecimal taxAmount,
            BigDecimal lineTotalAmount
    ) {
    }
}
