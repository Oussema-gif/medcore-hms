package com.medcore.hms.auth.service;

import com.medcore.hms.user.domain.UserAccount;
import com.medcore.hms.user.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {

    private UserAccountRepository repository;
    private CustomUserDetailsService service;
    private UserAccount userAccount;

    @BeforeEach
    void setUp() {
        repository = mock(UserAccountRepository.class);
        service = new CustomUserDetailsService(repository);
        userAccount = mock(UserAccount.class);
        when(userAccount.getUsername()).thenReturn("alice");
        when(userAccount.getPasswordHash()).thenReturn("encoded-password");
        when(userAccount.getRoleNames()).thenReturn(Set.of("ADMIN", "ROLE_CLINICIAN"));
    }

    @Test
    void loadsPrincipalAndNormalizesAuthorities() {
        when(repository.findByUsernameIgnoreCase("alice")).thenReturn(Optional.of(userAccount));

        CustomUserDetailsService.AccountPrincipal principal =
                (CustomUserDetailsService.AccountPrincipal) service.loadUserByUsername("alice");

        assertThat(principal.getUsername()).isEqualTo("alice");
        assertThat(principal.getPassword()).isEqualTo("encoded-password");
        assertThat(principal.getUserAccount()).isSameAs(userAccount);
        assertThat(principal.getAuthorities())
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_CLINICIAN");
        assertThat(principal.isEnabled()).isTrue();
    }

    @Test
    void missingUserRaisesUsernameNotFoundException() {
        when(repository.findByUsernameIgnoreCase("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User account was not found");
    }
}