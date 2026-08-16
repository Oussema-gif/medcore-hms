package com.medcore.hms.inventory.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record InventoryBatchResponse(
        Long id,
        Long medicineId,
        String lotNumber,
        LocalDate expiryDate,
        Instant receivedAt,
        BigDecimal initialQuantity,
        BigDecimal quantityOnHand,
        BigDecimal unitCost,
        String supplierReference,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
