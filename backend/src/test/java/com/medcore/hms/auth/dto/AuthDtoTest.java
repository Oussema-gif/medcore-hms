package com.medcore.hms.auth.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validLoginRequestHasNoConstraintViolations() {
        assertThat(validator.validate(new LoginRequest("alice", "correct-password"))).isEmpty();
    }

    @Test
    void blankLoginRequestReportsBothFields() {
        assertThat(validator.validate(new LoginRequest("", "")))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("usernameOrEmail", "password");
    }

    @Test
    void blankRefreshTokenIsRejected() {
        assertThat(validator.validate(new RefreshTokenRequest("")))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("refreshToken");
    }

    @Test
    void userSummaryDefensivelyCopiesRoles() {
        Set<String> roles = new HashSet<>();
        roles.add("ADMIN");
        UserSummary summary = new UserSummary(1L, "alice", "alice@example.com", "Alice", roles);
        roles.add("CLINICIAN");

        assertThat(summary.roles()).containsExactly("ADMIN");
        assertThatThrownBy(() -> summary.roles().add("NURSE"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void bearerFactoryCreatesExpectedTokenResponse() {
        TokenResponse response = TokenResponse.bearer("access", "refresh", 900);

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900);
    }
}