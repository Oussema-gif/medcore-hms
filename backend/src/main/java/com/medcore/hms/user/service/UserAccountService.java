package com.medcore.hms.user.service;

import com.medcore.hms.exception.ConflictException;
import com.medcore.hms.exception.ResourceNotFoundException;
import com.medcore.hms.security.AuthenticatedUser;
import com.medcore.hms.security.UserSecurityService;
import com.medcore.hms.user.domain.UserAccount;
import com.medcore.hms.user.domain.UserAccountStatus;
import com.medcore.hms.user.dto.CreateUserAccountRequest;
import com.medcore.hms.user.dto.UserAccountResponse;
import com.medcore.hms.user.mapper.UserAccountMapper;
import com.medcore.hms.user.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserAccountService implements UserSecurityService {

    private final UserAccountRepository userAccountRepository;
    private final UserAccountMapper userAccountMapper;
    private final Clock clock;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(
            UserAccountRepository userAccountRepository,
            UserAccountMapper userAccountMapper,
            Clock clock,
            PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.userAccountMapper = userAccountMapper;
        this.clock = clock;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<AuthenticatedUser> loadById(Long userId) {
        return userAccountRepository.findByIdAndDeletedAtIsNull(userId)
                .filter(account -> account.canAuthenticate(clock.instant()))
                .map(userAccountMapper::toAuthenticatedUser);
    }

    public UserAccount getRequired(Long userId) {
        return userAccountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account", userId));
    }

    public Optional<UserAccount> findByUsername(String username) {
        return userAccountRepository.findByUsernameIgnoreCaseAndDeletedAtIsNull(username);
    }

    public Optional<UserAccount> findByEmail(String email) {
        return userAccountRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email);
    }

    @Transactional
    public UserAccountResponse create(
            CreateUserAccountRequest request,
            Long actorUserId) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();
        if (userAccountRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("Username already exists");
        }
        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email already exists");
        }
        UserAccount userAccount = UserAccount.create();
        userAccount.setUsername(username);
        userAccount.setDisplayName(
                request.displayName() == null
                        ? null
                        : request.displayName().trim());
        userAccount.setEmail(email);
        userAccount.setPasswordHash(
                passwordEncoder.encode(request.password()));
        userAccount.setRole(request.role());
        userAccount.setStatus(UserAccountStatus.ACTIVE);
        userAccount.setPasswordChangedAt(clock.instant());
        userAccount.setCreatedByUserId(actorUserId);
        userAccount.setUpdatedByUserId(actorUserId);
        UserAccount saved = userAccountRepository.save(userAccount);
        return new UserAccountResponse(
                saved.getId(),
                saved.getUsername(),
                saved.getDisplayName(),
                saved.getEmail(),
                saved.getRole(),
                saved.getStatus(),
                saved.getCreatedAt(),
                saved.getUpdatedAt());
    }
}