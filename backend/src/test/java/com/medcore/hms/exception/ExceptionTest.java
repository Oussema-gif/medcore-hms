package com.medcore.hms.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionTest {

    @Test
    void unauthorizedExceptionPreservesMessageAndCause() {
        Throwable cause = new IllegalStateException("cause");
        UnauthorizedException exception = new UnauthorizedException("unauthorized", cause);

        assertThat(exception).hasMessage("unauthorized");
        assertThat(exception).hasCause(cause);
    }

    @Test
    void notFoundExceptionPreservesMessage() {
        NotFoundException exception = new NotFoundException("missing resource");

        assertThat(exception).hasMessage("missing resource");
    }

    @Test
    void conflictExceptionSupportsCause() {
        Throwable cause = new IllegalStateException("duplicate");
        ConflictException exception = new ConflictException("conflict", cause);

        assertThat(exception).hasMessage("conflict");
        assertThat(exception).hasCause(cause);
    }

    @Test
    void badRequestExceptionPreservesMessageAndCause() {
        Throwable cause = new IllegalArgumentException("malformed");
        BadRequestException exception = new BadRequestException("bad request", cause);

        assertThat(exception).hasMessage("bad request");
        assertThat(exception).hasCause(cause);
    }
}