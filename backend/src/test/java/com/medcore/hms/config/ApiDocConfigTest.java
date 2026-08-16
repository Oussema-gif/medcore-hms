package com.medcore.hms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiDocConfigTest {

    @Test
    void documentsApiMetadataAndBearerAuthentication() {
        OpenAPI openApi = new ApiDocConfig().medcoreOpenApi();

        assertThat(openApi.getInfo().getTitle()).isEqualTo("MedCore HMS API");
        assertThat(openApi.getInfo().getVersion()).isEqualTo("v1");
        assertThat(openApi.getComponents().getSecuritySchemes()).containsKey("bearerAuth");

        SecurityScheme scheme = openApi.getComponents().getSecuritySchemes().get("bearerAuth");
        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(scheme.getScheme()).isEqualTo("bearer");
        assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
        assertThat(openApi.getSecurity()).hasSize(1);
    }
}