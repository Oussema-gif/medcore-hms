package com.medcore.hms.patient.dto;

import com.medcore.hms.patient.domain.PatientSex;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreatePatientRequest(
        @NotBlank
        @Size(max = 32)
        String medicalRecordNumber,

        @NotBlank
        @Size(max = 128)
        String firstName,

        @NotBlank
        @Size(max = 128)
        String lastName,

        @NotNull
        @PastOrPresent
        LocalDate dateOfBirth,

        @NotNull
        PatientSex sex,

        @Email
        @Size(max = 254)
        String email,

        @Size(max = 32)
        String phoneNumber,

        @Size(max = 256)
        String addressLine1,

        @Size(max = 256)
        String addressLine2,

        @Size(max = 128)
        String city,

        @Size(max = 128)
        String state,

        @Size(max = 32)
        String postalCode,

        @Size(max = 128)
        String country,

        @Size(max = 256)
        String emergencyContactName,

        @Size(max = 32)
        String emergencyContactPhone,

        @Pattern(regexp = "A\\+|A-|B\\+|B-|AB\\+|AB-|O\\+|O-")
        String bloodGroup,

        @Size(max = 1024)
        String allergies,

        Long userAccountId
) {
}
