package com.medcore.hms.diagnosis.domain;

import com.medcore.hms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "diagnosis")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diagnosis extends BaseEntity {

    @Column(name = "medical_record_id", nullable = false)
    private Long medicalRecordId;

    @Column(name = "code_system", length = 64)
    private String codeSystem;

    @Column(name = "diagnosis_code", length = 64)
    private String diagnosisCode;

    @Column(nullable = false, length = 512)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "diagnosis_type", nullable = false, length = 16)
    private DiagnosisType diagnosisType = DiagnosisType.SECONDARY;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    public static Diagnosis create() {
        return new Diagnosis();
    }
}
