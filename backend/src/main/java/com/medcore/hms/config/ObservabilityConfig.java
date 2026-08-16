package com.medcore.hms.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.slf4j.MDC;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class ObservabilityConfig {

    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_MDC_KEY = "correlationId";

    @Bean
    public FilterRegistrationBean<Filter> correlationIdFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(this::filterRequest);
        registration.addUrlPatterns("/*");
        registration.setOrder(-100);
        return registration;
    }

    private void filterRequest(ServletRequest request, ServletResponse response, FilterChain chain)
            throws java.io.IOException, jakarta.servlet.ServletException {
        jakarta.servlet.http.HttpServletRequest httpRequest =
                (jakarta.servlet.http.HttpServletRequest) request;
        jakarta.servlet.http.HttpServletResponse httpResponse =
                (jakarta.servlet.http.HttpServletResponse) response;

        String correlationId = normalize(httpRequest.getHeader(CORRELATION_HEADER));
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(CORRELATION_MDC_KEY, correlationId);
        httpResponse.setHeader(CORRELATION_HEADER, correlationId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_MDC_KEY);
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank() || value.length() > 128) {
            return null;
        }
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}