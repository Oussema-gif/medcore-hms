package com.medcore.hms.patient.domain;

import com.medcore.hms.common.domain.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Patient extends SoftDeletableEntity {

    @Column(name = "user_account_id", unique = true)
    private Long userAccountId;

    @Column(name = "medical_record_number", nullable = false, length = 32, unique = true)
    private String medicalRecordNumber;

    @Column(name = "first_name", nullable = false, length = 128)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 128)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PatientSex sex;

    @Column(length = 254)
    private String email;

    @Column(name = "phone_number", length = 32)
    private String phoneNumber;

    @Column(name = "address_line1", length = 256)
    private String addressLine1;

    @Column(name = "address_line2", length = 256)
    private String addressLine2;

    @Column(length = 128)
    private String city;

    @Column(length = 128)
    private String state;

    @Column(name = "postal_code", length = 32)
    private String postalCode;

    @Column(length = 128)
    private String country;

    @Column(name = "emergency_contact_name", length = 256)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 32)
    private String emergencyContactPhone;

    @Column(name = "blood_group", length = 8)
    private String bloodGroup;

    @Column(length = 1024)
    private String allergies;

    public static Patient create() {
        return new Patient();
    }
}