package com.medcore.hms.inventory.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stock_movement")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inventory_batch_id", nullable = false)
    private Long inventoryBatchId;

    @Column(name = "movement_type", nullable = false, length = 32)
    private String movementType;

    @Column(name = "quantity_delta", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityDelta;

    @Column(name = "quantity_after", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityAfter;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "reference_type", length = 64)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "prescription_item_id")
    private Long prescriptionItemId;

    @Column(length = 512)
    private String reason;

    @Column(name = "performed_by_user_id", nullable = false)
    private Long performedByUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static StockMovement receipt(
            Long batchId,
            BigDecimal quantity,
            BigDecimal quantityAfter,
            Long performedByUserId) {

        Instant now = Instant.now();

        StockMovement movement = new StockMovement();
        movement.inventoryBatchId = batchId;
        movement.movementType = "RECEIPT";
        movement.quantityDelta = quantity;
        movement.quantityAfter = quantityAfter;
        movement.occurredAt = now;
        movement.performedByUserId = performedByUserId;
        movement.createdAt = now;

        return movement;
    }

    public static StockMovement adjustment(
            Long batchId,
            BigDecimal quantityDelta,
            BigDecimal quantityAfter,
            String reason,
            Long performedByUserId) {
        Instant now = Instant.now();
        StockMovement movement = new StockMovement();
        movement.inventoryBatchId = batchId;
        movement.movementType =
                quantityDelta.signum() > 0
                        ? "ADJUSTMENT_IN"
                        : "ADJUSTMENT_OUT";
        movement.quantityDelta = quantityDelta;
        movement.quantityAfter = quantityAfter;
        movement.occurredAt = now;
        movement.reason = reason;
        movement.performedByUserId = performedByUserId;
        movement.createdAt = now;
        return movement;
    }

    public static StockMovement dispense(
            Long batchId,
            BigDecimal quantityDelta,
            BigDecimal quantityAfter,
            Long prescriptionId,
            Long prescriptionItemId,
            Long performedByUserId) {
        Instant now = Instant.now();
        StockMovement movement = new StockMovement();
        movement.inventoryBatchId = batchId;
        movement.movementType = "DISPENSE";
        movement.quantityDelta = quantityDelta;
        movement.quantityAfter = quantityAfter;
        movement.occurredAt = now;
        movement.referenceType = "PRESCRIPTION";
        movement.referenceId = prescriptionId;
        movement.prescriptionItemId = prescriptionItemId;
        movement.performedByUserId = performedByUserId;
        movement.createdAt = now;
        return movement;
    }
}