package com.medcore.hms.vitalsign.domain;

import com.medcore.hms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "vital_sign")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VitalSign extends BaseEntity {

    @Column(name = "medical_record_id", nullable = false)
    private Long medicalRecordId;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "temperature_celsius", precision = 5, scale = 2)
    private BigDecimal temperatureCelsius;

    @Column(name = "systolic_bp")
    private Short systolicBp;

    @Column(name = "diastolic_bp")
    private Short diastolicBp;

    @Column(name = "heart_rate")
    private Short heartRate;

    @Column(name = "respiratory_rate")
    private Short respiratoryRate;

    @Column(name = "oxygen_saturation", precision = 5, scale = 2)
    private BigDecimal oxygenSaturation;

    @Column(name = "weight_kg", precision = 7, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "height_cm", precision = 7, scale = 2)
    private BigDecimal heightCm;

    public static VitalSign create() {
        return new VitalSign();
    }
}
