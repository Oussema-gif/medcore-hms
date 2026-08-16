package com.medcore.hms.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ApiException extends RuntimeException {

    private final String errorCode;
    private final int status;
    private final Map<String, Object> details;

    public ApiException(String errorCode, int status, String message) {
        this(errorCode, status, message, Map.of());
    }

    public ApiException(String errorCode, int status, String message, Map<String, Object> details) {
        super(message);
        if (errorCode == null || errorCode.isBlank()) {
            throw new IllegalArgumentException("Error code must not be blank");
        }
        if (status < 400 || status > 599) {
            throw new IllegalArgumentException("API exception status must be between 400 and 599");
        }
        this.errorCode = errorCode;
        this.status = status;
        this.details = details == null ? Map.of() : Map.copyOf(details);
    }
}