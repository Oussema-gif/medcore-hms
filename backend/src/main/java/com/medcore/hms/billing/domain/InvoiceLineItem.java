package com.medcore.hms.billing.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "invoice_line_item")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvoiceLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @Column(name = "item_type", nullable = false, length = 64)
    private String itemType;

    @Column(nullable = false, length = 512)
    private String description;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "line_total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal lineTotalAmount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static InvoiceLineItem create() {
        Instant now = Instant.now();

        InvoiceLineItem item = new InvoiceLineItem();
        item.createdAt = now;
        item.updatedAt = now;

        return item;
    }
}