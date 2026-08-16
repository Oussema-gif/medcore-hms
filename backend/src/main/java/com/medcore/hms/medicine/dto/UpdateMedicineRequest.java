package com.medcore.hms.medicine.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateMedicineRequest(
        @Size(max = 64)
        String medicineCode,

        @Size(max = 256)
        String name,

        @Size(max = 256)
        String genericName,

        @Size(max = 64)
        String dosageForm,

        @Size(max = 128)
        String strength,

        @Size(max = 32)
        String unit,

        @DecimalMin("0.0")
        BigDecimal reorderLevel
) {
}
