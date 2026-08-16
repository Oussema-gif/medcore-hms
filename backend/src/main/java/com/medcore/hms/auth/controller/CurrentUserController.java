package com.medcore.hms.auth.controller;

import com.medcore.hms.auth.dto.UserSummary;
import com.medcore.hms.exception.NotFoundException;
import com.medcore.hms.user.domain.UserAccount;
import com.medcore.hms.user.repository.UserAccountRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class CurrentUserController {

    private final UserAccountRepository userAccountRepository;

    public CurrentUserController(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping("/me")
    public UserSummary currentUser(Authentication authentication) {
        UserAccount userAccount = userAccountRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new NotFoundException("Authenticated user was not found"));

        return new UserSummary(
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.getEmail(),
                userAccount.getDisplayName(),
                userAccount.getRoleNames());
    }
}