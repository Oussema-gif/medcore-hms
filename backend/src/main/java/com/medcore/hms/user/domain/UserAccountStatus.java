package com.medcore.hms.user.domain;

public enum UserAccountStatus {
    ACTIVE,
    LOCKED,
    DISABLED,
    PENDING_RESET;

    public boolean canAuthenticate() {
        return this == ACTIVE;
    }
}