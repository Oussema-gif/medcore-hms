package com.medcore.hms.auth.dto;

import java.util.Set;

public record UserSummary(
        Long id,
        String username,
        String email,
        String displayName,
        Set<String> roles
) {

    public UserSummary {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }
}