package com.medcore.hms.doctoravailability.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateDoctorAvailabilityRequest(
        @Min(1)
        @Max(7)
        Short dayOfWeek,

        LocalTime startTime,

        LocalTime endTime,

        LocalDate effectiveFrom,

        LocalDate effectiveUntil
) {
}
