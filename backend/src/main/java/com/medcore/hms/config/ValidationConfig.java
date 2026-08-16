package com.medcore.hms.config;

import jakarta.validation.Validator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@Configuration(proxyBeanMethods = false)
public class ValidationConfig {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static MethodValidationPostProcessor methodValidationPostProcessor(
            Validator validator) {

        MethodValidationPostProcessor processor =
                new MethodValidationPostProcessor();

        processor.setValidator(validator);

        return processor;
    }
}