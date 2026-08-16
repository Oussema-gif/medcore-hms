package com.medcore.hms.config;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ClockConfigTest {

    @Test
    void createsUtcApplicationClock() {
        Clock clock = new ClockConfig().applicationClock();

        assertThat(clock.getZone()).isEqualTo(ZoneOffset.UTC);
        assertThat(clock.instant()).isBeforeOrEqualTo(Instant.now());
    }
}