package com.medcore.hms.security;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityPropertiesTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void bindsJwtDurationsAndAcceptsStrongSecret() {
        SecurityProperties properties = new SecurityProperties();
        properties.getJwt().setSecret("01234567890123456789012345678901");
        properties.getJwt().setAccessExpiration(Duration.ofMinutes(10));
        properties.getJwt().setRefreshExpiration(Duration.ofDays(14));

        assertThat(validator.validate(properties)).isEmpty();
        assertThat(properties.getJwt().getAccessExpiration()).isEqualTo(Duration.ofMinutes(10));
        assertThat(properties.getJwt().getRefreshExpiration()).isEqualTo(Duration.ofDays(14));
    }

    @Test
    void rejectsShortJwtSecret() {
        SecurityProperties properties = new SecurityProperties();
        properties.getJwt().setSecret("too-short");

        assertThat(validator.validate(properties))
                .extracting(violation -> violation.getMessage())
                .contains("JWT secret must be at least 32 UTF-8 bytes");
    }

    @Test
    void rejectsMissingJwtSecret() {
        SecurityProperties properties = new SecurityProperties();

        assertThat(validator.validate(properties))
                .extracting(violation -> violation.getMessage())
                .contains("must not be blank");
    }
}