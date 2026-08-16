package com.medcore.hms.billing.domain;

import com.medcore.hms.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Column(name = "payment_number", nullable = false, unique = true, length = 64)
    private String paymentNumber;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 32)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_reference", length = 256)
    private String paymentReference;

    @Column(name = "paid_at", nullable = false)
    private Instant paidAt = Instant.now();

    @Column(name = "recorded_by_user_id", nullable = false)
    private Long recordedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentStatus status = PaymentStatus.RECORDED;

    @Column(name = "voided_at")
    private Instant voidedAt;

    @Column(name = "void_reason", length = 512)
    private String voidReason;

    public static Payment create() {
        Payment payment = new Payment();
        payment.paidAt = Instant.now();
        return payment;
    }
}