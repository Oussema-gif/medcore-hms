package com.medcore.hms.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordConfigTest {

    @Test
    void createsWorkingBcryptPasswordEncoder() {
        PasswordEncoder encoder = new PasswordConfig().passwordEncoder();
        String encoded = encoder.encode("correct-password");

        assertThat(encoded).startsWith("$2");
        assertThat(encoded).isNotEqualTo("correct-password");
        assertThat(encoder.matches("correct-password", encoded)).isTrue();
        assertThat(encoder.matches("wrong-password", encoded)).isFalse();
    }
}