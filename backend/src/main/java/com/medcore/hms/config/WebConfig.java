package com.medcore.hms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig {

    private final List<String> allowedOrigins;
    private final List<String> allowedMethods;
    private final List<String> allowedHeaders;
    private final List<String> exposedHeaders;
    private final boolean allowCredentials;
    private final long maxAgeSeconds;

    public WebConfig(
            @Value("${app.cors.allowed-origins}") String configuredOrigins,
            @Value("${app.cors.allowed-methods}") String configuredMethods,
            @Value("${app.cors.allowed-headers}") String configuredHeaders,
            @Value("${app.cors.exposed-headers}") String configuredExposedHeaders,
            @Value("${app.cors.allow-credentials:false}") boolean allowCredentials,
            @Value("${app.cors.max-age-seconds:3600}") long maxAgeSeconds) {

        this.allowedOrigins = parseCsv(configuredOrigins);
        this.allowedMethods = parseCsv(configuredMethods);
        this.allowedHeaders = parseCsv(configuredHeaders);
        this.exposedHeaders = parseCsv(configuredExposedHeaders);
        this.allowCredentials = allowCredentials;
        this.maxAgeSeconds = maxAgeSeconds;

        validateConfiguration();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(allowedMethods);
        configuration.setAllowedHeaders(allowedHeaders);
        configuration.setExposedHeaders(exposedHeaders);
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAgeSeconds);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }

    private List<String> parseCsv(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private void validateConfiguration() {
        if (allowedOrigins.isEmpty()) {
            throw new IllegalStateException(
                    "At least one CORS allowed origin must be configured");
        }

        if (allowedMethods.isEmpty()) {
            throw new IllegalStateException(
                    "At least one CORS allowed method must be configured");
        }

        if (allowedHeaders.isEmpty()) {
            throw new IllegalStateException(
                    "At least one CORS allowed header must be configured");
        }

        if (maxAgeSeconds < 0) {
            throw new IllegalStateException(
                    "CORS max-age must not be negative");
        }

        if (allowedOrigins.contains("*") && allowCredentials) {
            throw new IllegalStateException(
                    "Wildcard CORS origins cannot be used with credentials");
        }
    }
}