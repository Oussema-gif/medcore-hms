package com.medcore.hms.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class WebConfigTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(WebConfig.class)
                    .withPropertyValues(
                            "app.cors.allowed-origins=https://app.medcore.example,https://admin.medcore.example",
                            "app.cors.allowed-methods=GET,POST,PUT,PATCH,DELETE,OPTIONS",
                            "app.cors.allowed-headers=Authorization,Content-Type,Accept,X-Request-Id",
                            "app.cors.exposed-headers=X-Request-Id",
                            "app.cors.allow-credentials=false",
                            "app.cors.max-age-seconds=1800");

    @Test
    void createsCorsConfigurationFromApplicationProperties() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(CorsConfigurationSource.class);

            CorsConfigurationSource source =
                    context.getBean(CorsConfigurationSource.class);

            HttpServletRequest request =
                    new org.springframework.mock.web.MockHttpServletRequest(
                            "GET",
                            "/api/v1/patients");

            CorsConfiguration configuration =
                    source.getCorsConfiguration(request);

            assertThat(configuration).isNotNull();
            assertThat(configuration.getAllowedOrigins())
                    .containsExactly(
                            "https://app.medcore.example",
                            "https://admin.medcore.example");
            assertThat(configuration.getAllowedMethods())
                    .containsExactly(
                            "GET",
                            "POST",
                            "PUT",
                            "PATCH",
                            "DELETE",
                            "OPTIONS");
            assertThat(configuration.getAllowedHeaders())
                    .containsExactly(
                            "Authorization",
                            "Content-Type",
                            "Accept",
                            "X-Request-Id");
            assertThat(configuration.getExposedHeaders())
                    .containsExactly("X-Request-Id");
            assertThat(configuration.getAllowCredentials())
                    .isFalse();
            assertThat(configuration.getMaxAge())
                    .isEqualTo(1800L);
        });
    }

    @Test
    void registersCorsConfigurationOnlyForApiPaths() {
        contextRunner.run(context -> {
            CorsConfigurationSource source =
                    context.getBean(CorsConfigurationSource.class);

            HttpServletRequest apiRequest =
                    new org.springframework.mock.web.MockHttpServletRequest(
                            "GET",
                            "/api/v1/patients");

            HttpServletRequest nonApiRequest =
                    new org.springframework.mock.web.MockHttpServletRequest(
                            "GET",
                            "/internal/resource");

            assertThat(source.getCorsConfiguration(apiRequest))
                    .isNotNull();
            assertThat(source.getCorsConfiguration(nonApiRequest))
                    .isNull();
        });
    }

    @Test
    void rejectsWildcardOriginsWhenCredentialsAreEnabled() {
        new ApplicationContextRunner()
                .withUserConfiguration(WebConfig.class)
                .withPropertyValues(
                        "app.cors.allowed-origins=*",
                        "app.cors.allowed-methods=GET,OPTIONS",
                        "app.cors.allowed-headers=Authorization,Content-Type",
                        "app.cors.exposed-headers=X-Request-Id",
                        "app.cors.allow-credentials=true",
                        "app.cors.max-age-seconds=3600")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(
                                    IllegalStateException.class)
                            .hasRootCauseMessage(
                                    "Wildcard CORS origins cannot be used with credentials");
                });
    }

    @Test
    void rejectsNegativeMaxAge() {
        new ApplicationContextRunner()
                .withUserConfiguration(WebConfig.class)
                .withPropertyValues(
                        "app.cors.allowed-origins=https://app.medcore.example",
                        "app.cors.allowed-methods=GET,OPTIONS",
                        "app.cors.allowed-headers=Authorization,Content-Type",
                        "app.cors.exposed-headers=X-Request-Id",
                        "app.cors.allow-credentials=false",
                        "app.cors.max-age-seconds=-1")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(
                                    IllegalStateException.class)
                            .hasRootCauseMessage(
                                    "CORS max-age must not be negative");
                });
    }
}