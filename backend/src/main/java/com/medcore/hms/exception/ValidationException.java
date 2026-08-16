package com.medcore.hms.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ValidationException extends ApiException {

    public ValidationException(String message, Map<String, Object> details) {
        super("VALIDATION_ERROR", HttpStatus.BAD_REQUEST.value(), message, details);
    }

    public ValidationException(String field, String message) {
        this(
                "Validation failed",
                Map.of(field, message));
    }
}