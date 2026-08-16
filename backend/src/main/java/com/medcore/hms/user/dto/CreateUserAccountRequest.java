package com.medcore.hms.user.dto;

import com.medcore.hms.user.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserAccountRequest(
        @NotBlank
        @Size(max = 64)
        String username,

        @Size(max = 160)
        String displayName,

        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @NotBlank
        @Size(min = 8, max = 128)
        String password,

        @NotNull
        UserRole role
) {
}
