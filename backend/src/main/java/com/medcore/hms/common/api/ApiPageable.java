package com.medcore.hms.common.api;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record ApiPageable(
        int page,
        int size
) {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public ApiPageable {
        if (page < 0) {
            throw new IllegalArgumentException("Page must not be negative");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException("Page size must be between 1 and " + MAX_SIZE);
        }
    }

    public static ApiPageable of(Integer page, Integer size) {
        int normalizedPage = page == null ? DEFAULT_PAGE : page;
        int normalizedSize = size == null ? DEFAULT_SIZE : size;
        return new ApiPageable(normalizedPage, normalizedSize);
    }

    public Pageable toPageable(String allowedSortProperty, Sort.Direction direction) {
        if (allowedSortProperty == null || allowedSortProperty.isBlank()) {
            throw new IllegalArgumentException("Sort property must be provided");
        }
        Sort.Direction normalizedDirection = direction == null ? Sort.Direction.ASC : direction;
        return PageRequest.of(page, size, Sort.by(normalizedDirection, allowedSortProperty));
    }

    public Pageable toPageable(String allowedSortProperty) {
        return toPageable(allowedSortProperty, Sort.Direction.ASC);
    }
}