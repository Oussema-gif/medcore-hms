package com.medcore.hms.user.repository;

import com.medcore.hms.user.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsernameIgnoreCaseAndDeletedAtIsNull(String username);

    Optional<UserAccount> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    Optional<UserAccount> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    Optional<UserAccount> findByUsernameIgnoreCase(String username);

    Optional<UserAccount> findByUsernameIgnoreCaseOrEmailIgnoreCase(
            String username,
            String email);

    @Query("""
            select userAccount
            from UserAccount userAccount
            where upper(userAccount.username) = upper(:username)
               or upper(userAccount.email) = upper(:email)
            """)
    Optional<UserAccount> findByUsernameOrEmailIgnoreCase(
            @Param("username") String username,
            @Param("email") String email);
}