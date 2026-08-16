package com.medcore.hms.appointment.domain;

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

import java.time.Instant;

@Entity
@Table(name = "appointment")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Appointment extends BaseEntity {

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "scheduled_start_at", nullable = false)
    private Instant scheduledStartAt;

    @Column(name = "scheduled_end_at", nullable = false)
    private Instant scheduledEndAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Column(nullable = false, length = 512)
    private String reason;

    @Column(name = "scheduling_notes", length = 1024)
    private String schedulingNotes;

    @Column(name = "check_in_at")
    private Instant checkInAt;

    @Column(name = "queue_position")
    private Integer queuePosition;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancellation_reason", length = 512)
    private String cancellationReason;

    public static Appointment create() {
        return new Appointment();
    }
}