package com.medcore.hms.auth.controller;

import com.medcore.hms.exception.GlobalExceptionHandler;
import com.medcore.hms.user.domain.UserAccount;
import com.medcore.hms.user.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CurrentUserControllerTest {

    private MockMvc mockMvc;
    private UserAccountRepository repository;
    private UserAccount userAccount;

    @BeforeEach
    void setUp() {
        repository = mock(UserAccountRepository.class);
        userAccount = mock(UserAccount.class);
        when(userAccount.getId()).thenReturn(1L);
        when(userAccount.getUsername()).thenReturn("alice");
        when(userAccount.getEmail()).thenReturn("alice@example.com");
        when(userAccount.getDisplayName()).thenReturn("Alice");
        when(userAccount.getRoleNames()).thenReturn(Set.of("ADMIN"));

        mockMvc = MockMvcBuilders
                .standaloneSetup(new CurrentUserController(repository))
                .setControllerAdvice(new GlobalExceptionHandler(
                        Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC)))
                .build();
    }

    @Test
    void returnsAuthenticatedUserSummary() throws Exception {
        when(repository.findByUsernameIgnoreCase("alice")).thenReturn(Optional.of(userAccount));
        Authentication authentication = new UsernamePasswordAuthenticationToken("alice", null);

        mockMvc.perform(get("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"));
    }

    @Test
    void missingAuthenticatedUserReturns404() throws Exception {
        when(repository.findByUsernameIgnoreCase("alice")).thenReturn(Optional.empty());
        Authentication authentication = new UsernamePasswordAuthenticationToken("alice", null);

        mockMvc.perform(get("/api/v1/users/me").principal(authentication))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Authenticated user was not found"));
    }
}