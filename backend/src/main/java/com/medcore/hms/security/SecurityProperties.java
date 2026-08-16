package com.medcore.hms.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ConfigurationProperties(prefix = "app.security")
@Validated
public class SecurityProperties {

    @Valid
    @NotNull
    private Jwt jwt = new Jwt();

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public static class Jwt {

        @NotBlank
        private String secret;

        @NotBlank
        private String issuer = "medcore-hms";

        @NotNull
        private Duration accessExpiration = Duration.ofMinutes(15);

        @NotNull
        private Duration refreshExpiration = Duration.ofDays(30);

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public Duration getAccessExpiration() {
            return accessExpiration;
        }

        public void setAccessExpiration(Duration accessExpiration) {
            this.accessExpiration = accessExpiration;
        }

        public Duration getRefreshExpiration() {
            return refreshExpiration;
        }

        public void setRefreshExpiration(Duration refreshExpiration) {
            this.refreshExpiration = refreshExpiration;
        }

        @AssertTrue(message = "JWT secret must be at least 32 UTF-8 bytes")
        public boolean isSecretStrongEnough() {
            return secret != null
                    && secret.getBytes(StandardCharsets.UTF_8).length >= 32;
        }
    }
}
