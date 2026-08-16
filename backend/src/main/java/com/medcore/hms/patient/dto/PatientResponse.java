package com.medcore.hms.patient.dto;

import com.medcore.hms.patient.domain.PatientSex;

import java.time.Instant;
import java.time.LocalDate;

public record PatientResponse(
        Long id,
        Long userAccountId,
        String medicalRecordNumber,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        PatientSex sex,
        String email,
        String phoneNumber,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        String country,
        String emergencyContactName,
        String emergencyContactPhone,
        String bloodGroup,
        String allergies,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
