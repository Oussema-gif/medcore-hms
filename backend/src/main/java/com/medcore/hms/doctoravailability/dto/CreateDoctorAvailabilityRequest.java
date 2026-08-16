package com.medcore.hms.doctoravailability.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateDoctorAvailabilityRequest(
        @NotNull
        Long doctorId,

        @NotNull
        @Min(1)
        @Max(7)
        Short dayOfWeek,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime,

        @NotNull
        LocalDate effectiveFrom,

        LocalDate effectiveUntil
) {
}
