package com.medcore.hms.config;

import com.medcore.hms.user.domain.UserAccount;
import com.medcore.hms.user.domain.UserAccountStatus;
import com.medcore.hms.user.domain.UserRole;
import com.medcore.hms.user.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

@Component
@Profile({"dev", "local"})
@EnableConfigurationProperties(DevelopmentAdminBootstrapProperties.class)
public class DevelopmentAdminBootstrap implements CommandLineRunner {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DevelopmentAdminBootstrap.class);

    private final DevelopmentAdminBootstrapProperties properties;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public DevelopmentAdminBootstrap(
            DevelopmentAdminBootstrapProperties properties,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            Clock clock) {
        this.properties = properties;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!properties.isEnabled()) {
            return;
        }

        BootstrapValues values = validateAndNormalizeProperties();

        UserAccount existingByUsername =
                userAccountRepository
                        .findByUsernameIgnoreCaseAndDeletedAtIsNull(
                                values.username())
                        .orElse(null);

        UserAccount existingByEmail =
                userAccountRepository
                        .findByEmailIgnoreCaseAndDeletedAtIsNull(
                                values.email())
                        .orElse(null);

        if (existingByUsername != null || existingByEmail != null) {
            validateExistingAccount(
                    existingByUsername,
                    existingByEmail,
                    values);

            LOGGER.info(
                    "Development administrator bootstrap skipped because the configured account already exists");

            return;
        }

        Instant now = clock.instant();

        UserAccount administrator = UserAccount.create();
        administrator.setUsername(values.username());
        administrator.setDisplayName(values.displayName());
        administrator.setEmail(values.email());
        administrator.setPasswordHash(
                passwordEncoder.encode(values.password()));
        administrator.setRole(UserRole.ADMINISTRATOR);
        administrator.setStatus(UserAccountStatus.ACTIVE);
        administrator.setPasswordChangedAt(now);

        userAccountRepository.saveAndFlush(administrator);

        LOGGER.info(
                "Development administrator account created with username '{}'",
                values.username());
    }

    private BootstrapValues validateAndNormalizeProperties() {
        String username = normalizeRequired(
                properties.getUsername(),
                "app.bootstrap.admin.username");

        String email = normalizeRequired(
                properties.getEmail(),
                "app.bootstrap.admin.email")
                .toLowerCase(Locale.ROOT);

        String password = properties.getPassword();

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "app.bootstrap.admin.password must be configured when development administrator bootstrap is enabled");
        }

        if (password.length() < 8) {
            throw new IllegalStateException(
                    "app.bootstrap.admin.password must contain at least 8 characters");
        }

        String displayName = normalizeOptional(
                properties.getDisplayName());

        return new BootstrapValues(
                username,
                email,
                displayName,
                password);
    }

    private void validateExistingAccount(
            UserAccount existingByUsername,
            UserAccount existingByEmail,
            BootstrapValues values) {

        if (existingByUsername != null
                && !existingByUsername.getEmail()
                        .equalsIgnoreCase(values.email())) {
            throw new IllegalStateException(
                    "The configured bootstrap username belongs to a different email address");
        }

        if (existingByEmail != null
                && !existingByEmail.getUsername()
                        .equalsIgnoreCase(values.username())) {
            throw new IllegalStateException(
                    "The configured bootstrap email belongs to a different username");
        }

        UserAccount existingAccount =
                existingByUsername != null
                        ? existingByUsername
                        : existingByEmail;

        if (existingAccount.getRole() != UserRole.ADMINISTRATOR) {
            throw new IllegalStateException(
                    "The configured bootstrap account exists but is not an administrator");
        }

        if (existingAccount.getStatus() != UserAccountStatus.ACTIVE
                || existingAccount.getDeletedAt() != null) {
            throw new IllegalStateException(
                    "The configured bootstrap administrator exists but is not active");
        }
    }

    private String normalizeRequired(
            String value,
            String propertyName) {

        String normalized = normalizeOptional(value);

        if (normalized == null) {
            throw new IllegalStateException(
                    propertyName
                            + " must be configured when development administrator bootstrap is enabled");
        }

        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private record BootstrapValues(
            String username,
            String email,
            String displayName,
            String password) {
    }
}