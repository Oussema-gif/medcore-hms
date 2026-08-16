package com.medcore.hms.doctoravailability.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record DoctorAvailabilityResponse(
        Long id,
        Long doctorId,
        Short dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        LocalDate effectiveFrom,
        LocalDate effectiveUntil,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
