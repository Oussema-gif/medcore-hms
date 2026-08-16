package com.medcore.hms.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerValidationTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler(
                Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC));
        request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
    }

    @Test
    void mapsMethodArgumentValidationTo400WithFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("loginRequest", "usernameOrEmail", "Username or email is required"),
                new FieldError("loginRequest", "password", "Password is required")));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                mock(MethodParameter.class), bindingResult);

        ResponseEntity<ApiError> response = handler.handleValidation(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message()).isEqualTo("Validation failed");
        assertThat(response.getBody().fieldErrors())
                .extracting(ApiError.FieldViolation::field)
                .containsExactly("usernameOrEmail", "password");
        assertThat(response.getBody().fieldErrors())
                .extracting(ApiError.FieldViolation::message)
                .containsExactly("Username or email is required", "Password is required");
    }
}