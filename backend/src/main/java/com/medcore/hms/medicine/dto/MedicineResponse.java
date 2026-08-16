package com.medcore.hms.medicine.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record MedicineResponse(
        Long id,
        String medicineCode,
        String name,
        String genericName,
        String dosageForm,
        String strength,
        String unit,
        BigDecimal reorderLevel,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
