package com.medcore.hms.prescription.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "prescription_item")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prescription_id", nullable = false)
    private Long prescriptionId;

    @Column(name = "medicine_id", nullable = false)
    private Long medicineId;

    @Column(nullable = false, length = 128)
    private String dosage;

    @Column(nullable = false, length = 64)
    private String route;

    @Column(nullable = false, length = 128)
    private String frequency;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "quantity_prescribed", nullable = false,
            precision = 19, scale = 4)
    private BigDecimal quantityPrescribed;

    @Column(length = 2048)
    private String instructions;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static PrescriptionItem create() {
        Instant now = Instant.now();

        PrescriptionItem item = new PrescriptionItem();
        item.createdAt = now;
        item.updatedAt = now;

        return item;
    }
}
