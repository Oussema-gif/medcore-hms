package com.medcore.hms.config;

import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ValidationConfig.class);

    @Test
    void registersValidatorAndMethodValidationProcessor() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(Validator.class);
            assertThat(context).hasSingleBean(MethodValidationPostProcessor.class);
            assertThat(context.getBean(Validator.class)).isNotNull();
        });
    }
}