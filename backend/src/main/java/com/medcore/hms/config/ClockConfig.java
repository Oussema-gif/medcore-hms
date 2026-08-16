package com.medcore.hms.config;

import com.medcore.hms.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class ClockConfig {

    @Bean
    public Clock applicationClock() {
        return Clock.systemUTC();
    }
}