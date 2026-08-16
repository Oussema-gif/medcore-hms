package com.medcore.hms.prescription.domain;

import com.medcore.hms.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "prescription")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Prescription extends BaseEntity {

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(name = "medical_record_id")
    private Long medicalRecordId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PrescriptionStatus status =
            PrescriptionStatus.ACTIVE;

    @Column(name = "prescribed_at", nullable = false)
    private Instant prescribedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(length = 2048)
    private String notes;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancellation_reason", length = 512)
    private String cancellationReason;

    public static Prescription create() {
        Prescription prescription = new Prescription();
        prescription.prescribedAt = Instant.now();
        return prescription;
    }
}
