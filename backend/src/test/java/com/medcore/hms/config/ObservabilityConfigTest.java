package com.medcore.hms.config;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ObservabilityConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ObservabilityConfig.class);

    @AfterEach
    void clearMdc() {
        org.slf4j.MDC.clear();
    }

    @Test
    void preservesSafeCorrelationIdAndReturnsItInResponse() throws Exception {
        contextRunner.run(context -> {
            FilterRegistrationBean<?> registration = context.getBean("correlationIdFilter", FilterRegistrationBean.class);
            Filter filter = (Filter) registration.getFilter();
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("X-Correlation-Id", "trace-123");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, new MockFilterChain());

            assertThat(response.getHeader("X-Correlation-Id")).isEqualTo("trace-123");
            assertThat(org.slf4j.MDC.get("correlationId")).isNull();
        });
    }

    @Test
    void generatesCorrelationIdWhenHeaderIsMissing() throws Exception {
        contextRunner.run(context -> {
            FilterRegistrationBean<?> registration = context.getBean("correlationIdFilter", FilterRegistrationBean.class);
            Filter filter = (Filter) registration.getFilter();
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(new MockHttpServletRequest(), response, new MockFilterChain());

            String correlationId = response.getHeader("X-Correlation-Id");
            assertThat(correlationId).isNotBlank();
            assertThat(correlationId).hasSize(36);
            assertThat(org.slf4j.MDC.get("correlationId")).isNull();
        });
    }
}