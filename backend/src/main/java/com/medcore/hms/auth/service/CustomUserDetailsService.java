package com.medcore.hms.auth.service;

import com.medcore.hms.user.domain.UserAccount;
import com.medcore.hms.user.repository.UserAccountRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Locale;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public CustomUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount userAccount = userAccountRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User account was not found"));
        return new AccountPrincipal(userAccount);
    }

    public static final class AccountPrincipal implements UserDetails {

        private final UserAccount userAccount;

        public AccountPrincipal(UserAccount userAccount) {
            this.userAccount = userAccount;
        }

        public UserAccount getUserAccount() {
            return userAccount;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return userAccount.getRoleNames().stream()
                    .map(role -> role.toUpperCase(Locale.ROOT).startsWith("ROLE_")
                            ? role.toUpperCase(Locale.ROOT)
                            : "ROLE_" + role.toUpperCase(Locale.ROOT))
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        }

        @Override
        public String getPassword() {
            return userAccount.getPasswordHash();
        }

        @Override
        public String getUsername() {
            return userAccount.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}