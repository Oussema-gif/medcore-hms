package com.medcore.hms.auth.config;
import com.medcore.hms.security.SecurityConfig;

import com.medcore.hms.auth.filter.JwtAuthenticationFilter;
import com.medcore.hms.config.PasswordConfig;
import com.medcore.hms.config.WebConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(
                            SecurityConfig.class,
                            PasswordConfig.class,
                            WebConfig.class)
                    .withBean(
                            JwtAuthenticationFilter.class,
                            () -> mock(JwtAuthenticationFilter.class))
                    .withBean(
                            UserDetailsService.class,
                            () -> mock(UserDetailsService.class))
                    .withPropertyValues(
                            "app.cors.allowed-origins=http://localhost:3000");

    @Test
    void createsStatelessSecurityInfrastructure() {
        contextRunner.run(context -> {
            assertThat(context)
                    .hasSingleBean(PasswordEncoder.class);

            assertThat(context)
                    .hasSingleBean(SecurityConfig.class);

            assertThat(context)
                    .hasBean("securityFilterChain");

            assertThat(context)
                    .hasBean("authenticationProvider");

            assertThat(context)
                    .hasBean("authenticationManager");
        });
    }

    @Test
    void exposesConfiguredCorsSource() {
        contextRunner.run(context -> {
            assertThat(context)
                    .hasBean("corsConfigurationSource");

            PasswordEncoder passwordEncoder =
                    context.getBean(PasswordEncoder.class);

            assertThat(passwordEncoder.matches(
                    "secret",
                    passwordEncoder.encode("secret")))
                    .isTrue();
        });
    }
}