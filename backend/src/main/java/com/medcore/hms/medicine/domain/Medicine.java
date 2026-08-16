package com.medcore.hms.medicine.domain;

import com.medcore.hms.common.domain.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "medicine")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Medicine extends SoftDeletableEntity {

    @Column(name = "medicine_code", nullable = false, unique = true, length = 64)
    private String medicineCode;

    @Column(nullable = false, length = 256)
    private String name;

    @Column(name = "generic_name", length = 256)
    private String genericName;

    @Column(name = "dosage_form", nullable = false, length = 64)
    private String dosageForm;

    @Column(length = 128)
    private String strength;

    @Column(nullable = false, length = 32)
    private String unit;

    @Column(name = "reorder_level", nullable = false, precision = 19, scale = 4)
    private BigDecimal reorderLevel = BigDecimal.ZERO;

    public static Medicine create() {
        return new Medicine();
    }
}
