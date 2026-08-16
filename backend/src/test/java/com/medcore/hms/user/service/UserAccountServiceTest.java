package com.medcore.hms.user.service;

import com.medcore.hms.exception.ConflictException;
import com.medcore.hms.exception.ResourceNotFoundException;
import com.medcore.hms.security.AuthenticatedUser;
import com.medcore.hms.user.domain.UserAccount;
import com.medcore.hms.user.domain.UserAccountStatus;
import com.medcore.hms.user.domain.UserRole;
import com.medcore.hms.user.dto.CreateUserAccountRequest;
import com.medcore.hms.user.dto.UserAccountResponse;
import com.medcore.hms.user.mapper.UserAccountMapper;
import com.medcore.hms.user.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserAccountServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-15T10:00:00Z");

    private UserAccountRepository userAccountRepository;
    private UserAccountMapper userAccountMapper;
    private PasswordEncoder passwordEncoder;
    private UserAccountService service;

    @BeforeEach
    void setUp() {
        userAccountRepository = mock(UserAccountRepository.class);
        userAccountMapper = mock(UserAccountMapper.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new UserAccountService(
                userAccountRepository,
                userAccountMapper,
                Clock.fixed(NOW, ZoneOffset.UTC),
                passwordEncoder);
    }

    @Test
    void createsUserAccountWithNormalizedUsernameAndEmail() {
        CreateUserAccountRequest request = new CreateUserAccountRequest(
                "  Alice  ",
                "  Alice Example  ",
                "  Alice@Example.com  ",
                "plain-password",
                UserRole.RECEPTIONIST);

        when(userAccountRepository.existsByUsernameIgnoreCase("Alice")).thenReturn(false);
        when(userAccountRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userAccountRepository.save(any(UserAccount.class)))
                .thenAnswer(invocation -> {
                    UserAccount saved = invocation.getArgument(0);
                    saved.setId(42L);
                    saved.setCreatedAt(NOW);
                    saved.setUpdatedAt(NOW);
                    return saved;
                });

        UserAccountResponse response = service.create(request, 1L);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.username()).isEqualTo("Alice");
        assertThat(response.displayName()).isEqualTo("Alice Example");
        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.role()).isEqualTo(UserRole.RECEPTIONIST);
        assertThat(response.status()).isEqualTo(UserAccountStatus.ACTIVE);

        verify(passwordEncoder).encode("plain-password");
        verify(userAccountRepository).save(any(UserAccount.class));
    }

    @Test
    void createAllowsNullDisplayName() {
        CreateUserAccountRequest request = new CreateUserAccountRequest(
                "bob",
                null,
                "bob@example.com",
                "plain-password",
                UserRole.DOCTOR);

        when(userAccountRepository.existsByUsernameIgnoreCase("bob")).thenReturn(false);
        when(userAccountRepository.existsByEmailIgnoreCase("bob@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userAccountRepository.save(any(UserAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserAccountResponse response = service.create(request, 1L);

        assertThat(response.displayName()).isNull();
    }

    @Test
    void rejectsDuplicateUsername() {
        CreateUserAccountRequest request = new CreateUserAccountRequest(
                "alice",
                "Alice",
                "alice@example.com",
                "plain-password",
                UserRole.RECEPTIONIST);

        when(userAccountRepository.existsByUsernameIgnoreCase("alice")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request, 1L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Username already exists");

        verify(userAccountRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void rejectsDuplicateEmail() {
        CreateUserAccountRequest request = new CreateUserAccountRequest(
                "alice",
                "Alice",
                "alice@example.com",
                "plain-password",
                UserRole.RECEPTIONIST);

        when(userAccountRepository.existsByUsernameIgnoreCase("alice")).thenReturn(false);
        when(userAccountRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request, 1L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email already exists");

        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void getRequiredReturnsAccountWhenPresentAndNotDeleted() {
        UserAccount account = mock(UserAccount.class);
        when(userAccountRepository.findByIdAndDeletedAtIsNull(7L)).thenReturn(Optional.of(account));

        UserAccount result = service.getRequired(7L);

        assertThat(result).isSameAs(account);
    }

    @Test
    void getRequiredThrowsWhenAccountMissing() {
        when(userAccountRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRequired(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByUsernameDelegatesToRepository() {
        UserAccount account = mock(UserAccount.class);
        when(userAccountRepository.findByUsernameIgnoreCaseAndDeletedAtIsNull("alice"))
                .thenReturn(Optional.of(account));

        Optional<UserAccount> result = service.findByUsername("alice");

        assertThat(result).contains(account);
    }

    @Test
    void findByEmailDelegatesToRepository() {
        when(userAccountRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("missing@example.com"))
                .thenReturn(Optional.empty());

        Optional<UserAccount> result = service.findByEmail("missing@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void loadByIdReturnsAuthenticatedUserWhenAccountCanAuthenticate() {
        UserAccount account = mock(UserAccount.class);
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(1L, "alice", "RECEPTIONIST", true, true);

        when(userAccountRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(account));
        when(account.canAuthenticate(NOW)).thenReturn(true);
        when(userAccountMapper.toAuthenticatedUser(account)).thenReturn(authenticatedUser);

        Optional<AuthenticatedUser> result = service.loadById(1L);

        assertThat(result).contains(authenticatedUser);
    }

    @Test
    void loadByIdReturnsEmptyWhenAccountCannotAuthenticate() {
        UserAccount account = mock(UserAccount.class);

        when(userAccountRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(account));
        when(account.canAuthenticate(NOW)).thenReturn(false);

        Optional<AuthenticatedUser> result = service.loadById(1L);

        assertThat(result).isEmpty();
        verify(userAccountMapper, never()).toAuthenticatedUser(any());
    }

    @Test
    void loadByIdReturnsEmptyWhenAccountMissing() {
        when(userAccountRepository.findByIdAndDeletedAtIsNull(404L)).thenReturn(Optional.empty());

        Optional<AuthenticatedUser> result = service.loadById(404L);

        assertThat(result).isEmpty();
    }
}