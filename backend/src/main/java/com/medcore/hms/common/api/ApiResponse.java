package com.medcore.hms.common.api;

import java.time.Instant;

public record ApiResponse<T>(
        T data,
        String message,
        Instant timestamp
) {

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message, Instant.now());
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation completed successfully");
    }
}