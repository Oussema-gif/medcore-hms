package com.medcore.hms.laboratory.domain;

import com.medcore.hms.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "laboratory_request")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LaboratoryRequest extends BaseEntity {

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "ordering_doctor_id", nullable = false)
    private Long orderingDoctorId;

    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(name = "medical_record_id")
    private Long medicalRecordId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private LaboratoryPriority priority = LaboratoryPriority.ROUTINE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LaboratoryRequestStatus status =
            LaboratoryRequestStatus.REQUESTED;

    @Column(name = "clinical_notes", length = 2048)
    private String clinicalNotes;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "sample_collected_at")
    private Instant sampleCollectedAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancellation_reason", length = 512)
    private String cancellationReason;

    public static LaboratoryRequest create() {
        LaboratoryRequest request = new LaboratoryRequest();
        request.requestedAt = Instant.now();
        return request;
    }
}
