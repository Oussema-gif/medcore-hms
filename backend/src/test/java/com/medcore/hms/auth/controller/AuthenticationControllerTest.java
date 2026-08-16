package com.medcore.hms.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medcore.hms.auth.dto.LoginRequest;
import com.medcore.hms.auth.dto.RefreshTokenRequest;
import com.medcore.hms.auth.dto.TokenResponse;
import com.medcore.hms.auth.dto.UserSummary;
import com.medcore.hms.auth.service.AuthenticationService;
import com.medcore.hms.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationControllerTest {

    private MockMvc mockMvc;
    private AuthenticationService authenticationService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authenticationService = mock(AuthenticationService.class);
        objectMapper = new ObjectMapper();
        AuthenticationController controller = new AuthenticationController(authenticationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(
                        Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC)))
                .build();
    }

    @Test
    void loginReturnsTokensAndUserSummary() throws Exception {
        AuthenticationService.AuthenticatedSession session =
                new AuthenticationService.AuthenticatedSession(
                        TokenResponse.bearer("access-token", "refresh-token", 900),
                        new UserSummary(1L, "alice", "alice@example.com", "Alice", Set.of("ADMIN")));
        when(authenticationService.authenticate(any(), eq("192.0.2.10"), eq("JUnit")))
                .thenReturn(session);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "JUnit")
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.10");
                            return request;
                        })
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("alice", "correct-password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokens.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokens.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("alice"));
    }

    @Test
    void invalidLoginRequestReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usernameOrEmail\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void logoutDelegatesRefreshToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("refresh-token"))))
                .andExpect(status().isNoContent());

        verify(authenticationService).logout("refresh-token");
    }
}