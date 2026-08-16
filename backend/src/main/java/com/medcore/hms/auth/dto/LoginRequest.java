package com.medcore.hms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Username or email is required")
        @Size(
                max = 254,
                message = "Username or email must not exceed 254 characters")
        String usernameOrEmail,

        @NotBlank(message = "Password is required")
        @Size(
                max = 128,
                message = "Password must not exceed 128 characters")
        String password
) {
}