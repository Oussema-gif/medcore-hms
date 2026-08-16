package com.medcore.hms.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class LocaleConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(LocaleConfig.class);

    @Test
    void registersEnglishDefaultAndLocaleChangeSupport() {
        contextRunner.run(context -> {
            LocaleResolver resolver = context.getBean(LocaleResolver.class);
            LocaleChangeInterceptor interceptor = context.getBean(LocaleChangeInterceptor.class);
            HttpServletRequest request = new MockHttpServletRequest();

            assertThat(resolver.resolveLocale(request)).isEqualTo(Locale.ENGLISH);
            assertThat(interceptor.getParamName()).isEqualTo("lang");
            assertThat(context.getBean(LocaleConfig.class).supportedLocales())
                    .containsExactly(Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN);
        });
    }
}