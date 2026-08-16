package com.medcore.hms.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        List<ApiError.FieldViolation> violations =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(this::toViolation)
                        .toList();

        return response(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                request,
                violations);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {

        List<ApiError.FieldViolation> violations =
                exception.getConstraintViolations()
                        .stream()
                        .map(violation ->
                                new ApiError.FieldViolation(
                                        violation.getPropertyPath()
                                                .toString(),
                                        violation.getMessage()))
                        .toList();

        return response(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                request,
                violations);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {

        String parameterName =
                exception.getName();

        String message =
                parameterName
                        + " must be a valid "
                        + expectedTypeName(exception);

        return response(
                HttpStatus.BAD_REQUEST,
                message,
                request,
                List.of());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(
            UnauthorizedException exception,
            HttpServletRequest request) {

        return response(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                request,
                List.of());
    }

    @ExceptionHandler({
            AuthenticationException.class,
            AccessDeniedException.class
    })
    public ResponseEntity<ApiError> handleSecurityException(
            RuntimeException exception,
            HttpServletRequest request) {

        HttpStatus status =
                exception instanceof AccessDeniedException
                        ? HttpStatus.FORBIDDEN
                        : HttpStatus.UNAUTHORIZED;

        return response(
                status,
                "Authentication or authorization failed",
                request,
                List.of());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            NotFoundException exception,
            HttpServletRequest request) {

        return response(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                List.of());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request) {

        return response(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                List.of());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflictException(
            ConflictException exception,
            HttpServletRequest request) {

        return response(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request,
                List.of());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(
            BadRequestException exception,
            HttpServletRequest request) {

        return response(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request,
                List.of());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request) {

        HttpStatus status =
                HttpStatus.valueOf(
                        exception.getStatusCode().value());

        String message =
                exception.getReason() == null
                        ? status.getReasonPhrase()
                        : exception.getReason();

        return response(
                status,
                message,
                request,
                List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {

        return response(
                HttpStatus.CONFLICT,
                "The requested resource conflicts with existing data",
                request,
                List.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        return response(
                HttpStatus.BAD_REQUEST,
                "Invalid request body",
                request,
                List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception exception,
            HttpServletRequest request) {

        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request,
                List.of());
    }

    private ApiError.FieldViolation toViolation(
            FieldError error) {

        return new ApiError.FieldViolation(
                error.getField(),
                error.getDefaultMessage());
    }

    private String expectedTypeName(
            MethodArgumentTypeMismatchException exception) {

        if (exception.getRequiredType() == null) {
            return "value";
        }

        return exception.getRequiredType()
                .getSimpleName()
                .toLowerCase();
    }

    private ResponseEntity<ApiError> response(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<ApiError.FieldViolation> violations) {

        ApiError error =
                new ApiError(
                        Instant.now(clock),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        request.getRequestURI(),
                        MDC.get("correlationId"),
                        violations);

        return ResponseEntity
                .status(status)
                .body(error);
    }
}