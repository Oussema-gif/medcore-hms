package com.medcore.hms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonConfigTest {

    @Test
    void serializesJavaTimeAsIsoAndOmitsNulls() throws Exception {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        new JacksonConfig().jacksonCustomizer().customize(builder);
        ObjectMapper mapper = builder.build();

        String json = mapper.writeValueAsString(new SamplePayload(
                Instant.parse("2026-01-15T10:00:00Z"), null));

        assertThat(json).contains("2026-01-15T10:00:00Z");
        assertThat(json).doesNotContain("optional");
        assertThat(json).doesNotContain("1705312800000");
    }

    private record SamplePayload(Instant createdAt, String optional) {
    }
}