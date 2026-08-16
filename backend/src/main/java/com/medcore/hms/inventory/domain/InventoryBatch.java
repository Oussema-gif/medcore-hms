package com.medcore.hms.inventory.domain;

import com.medcore.hms.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "inventory_batch")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryBatch extends BaseEntity {

    @Column(name = "medicine_id", nullable = false)
    private Long medicineId;

    @Column(name = "lot_number", nullable = false, length = 128)
    private String lotNumber;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "initial_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal initialQuantity;

    @Column(name = "quantity_on_hand", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityOnHand;

    @Column(name = "unit_cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitCost;

    @Column(name = "supplier_reference", length = 256)
    private String supplierReference;

    @Column(nullable = false)
    private boolean active = true;

    public static InventoryBatch create() {
        InventoryBatch batch = new InventoryBatch();
        batch.receivedAt = Instant.now();
        return batch;
    }
}
