package com.medcore.hms.auth.controller;

import com.medcore.hms.auth.dto.LoginRequest;
import com.medcore.hms.auth.dto.RefreshTokenRequest;
import com.medcore.hms.auth.dto.TokenResponse;
import com.medcore.hms.auth.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationService.AuthenticatedSession> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        AuthenticationService.AuthenticatedSession session = authenticationService.authenticate(
                request,
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(session);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        TokenResponse response = authenticationService.refresh(
                request.refreshToken(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authenticationService.logout(request.refreshToken());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}