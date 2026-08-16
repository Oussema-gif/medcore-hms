package com.medcore.hms.user.domain;

public enum UserRole {
    ADMINISTRATOR,
    DOCTOR,
    RECEPTIONIST,
    PATIENT;

    public String authority() {
        return "ROLE_" + name();
    }
}