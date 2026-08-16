package com.medcore.hms.medicalrecord.domain;

import com.medcore.hms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "medical_record")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicalRecord extends BaseEntity {

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(name = "encounter_at", nullable = false)
    private Instant encounterAt;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(name = "clinical_notes", nullable = false, columnDefinition = "TEXT")
    private String clinicalNotes;

    @Column(name = "patient_visible", nullable = false)
    private boolean patientVisible = false;

    @Column(name = "released_at")
    private Instant releasedAt;

    public static MedicalRecord create() {
        return new MedicalRecord();
    }
}
