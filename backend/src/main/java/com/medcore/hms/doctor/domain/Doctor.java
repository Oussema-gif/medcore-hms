package com.medcore.hms.doctor.domain;

import com.medcore.hms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "doctor")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Doctor extends BaseEntity {

    @Column(name = "user_account_id", nullable = false, unique = true)
    private Long userAccountId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(
            name = "professional_registration_number",
            nullable = false,
            length = 64,
            unique = true)
    private String professionalRegistrationNumber;

    @Column(name = "first_name", nullable = false, length = 128)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 128)
    private String lastName;

    @Column(length = 256)
    private String specialty;

    @Column(name = "phone_number", length = 32)
    private String phoneNumber;

    @Column(length = 254)
    private String email;

    @Column(name = "consultation_duration_minutes", nullable = false)
    private Integer consultationDurationMinutes = 15;

    @Column(nullable = false)
    private boolean active = true;

    public static Doctor create() {
        return new Doctor();
    }
}