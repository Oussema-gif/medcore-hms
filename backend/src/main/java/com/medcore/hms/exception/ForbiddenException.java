package com.medcore.hms.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {

    public ForbiddenException() {
        super(
                "FORBIDDEN",
                HttpStatus.FORBIDDEN.value(),
                "You do not have permission to access this resource");
    }

    public ForbiddenException(String message) {
        super("FORBIDDEN", HttpStatus.FORBIDDEN.value(), message);
    }
}