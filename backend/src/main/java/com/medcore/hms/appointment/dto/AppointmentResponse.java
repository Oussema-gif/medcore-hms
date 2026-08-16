package com.medcore.hms.appointment.dto;

import com.medcore.hms.appointment.domain.AppointmentStatus;

import java.time.Instant;

public record AppointmentResponse(
        Long id,
        Long patientId,
        Long doctorId,
        Long departmentId,
        Instant scheduledStartAt,
        Instant scheduledEndAt,
        AppointmentStatus status,
        String reason,
        String schedulingNotes,
        Instant checkInAt,
        Integer queuePosition,
        Instant cancelledAt,
        String cancellationReason,
        Instant createdAt,
        Instant updatedAt
) {
}
