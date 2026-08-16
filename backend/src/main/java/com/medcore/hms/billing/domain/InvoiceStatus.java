package com.medcore.hms.billing.domain;

public enum InvoiceStatus {
    DRAFT,
    ISSUED,
    PARTIALLY_PAID,
    PAID,
    VOID,
    OVERDUE
}
