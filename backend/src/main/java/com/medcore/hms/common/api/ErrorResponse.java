package com.medcore.hms.common.api;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, Object> details
) {

    public ErrorResponse {
        details = details == null ? Map.of() : Map.copyOf(details);
    }
}