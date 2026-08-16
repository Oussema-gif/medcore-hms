package com.medcore.hms.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String resourceName, Object resourceId) {
        super(
                "RESOURCE_NOT_FOUND",
                HttpStatus.NOT_FOUND.value(),
                resourceName + " was not found",
                java.util.Map.of(
                        "resource", resourceName,
                        "resourceId", String.valueOf(resourceId)));
    }
}