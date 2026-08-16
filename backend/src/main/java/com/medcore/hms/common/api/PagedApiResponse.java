package com.medcore.hms.common.api;

import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;

public record PagedApiResponse<T>(
        List<T> data,
        int page,
        int size,
        long totalElements,
        int totalPages,
        String message,
        Instant timestamp) {

    public PagedApiResponse {
        data = data == null ? List.of() : List.copyOf(data);
        message = message == null || message.isBlank()
                ? "Operation completed successfully"
                : message;
        timestamp = timestamp == null
                ? Instant.now()
                : timestamp;
    }

    public static <T> PagedApiResponse<T> from(
            PageResponse<T> pageResponse,
            String message) {

        return new PagedApiResponse<>(
                pageResponse.data(),
                pageResponse.page(),
                pageResponse.size(),
                pageResponse.totalElements(),
                pageResponse.totalPages(),
                message,
                Instant.now());
    }

    public static <T> PagedApiResponse<T> from(
            Page<T> page,
            String message) {

        return from(PageResponse.from(page), message);
    }
}