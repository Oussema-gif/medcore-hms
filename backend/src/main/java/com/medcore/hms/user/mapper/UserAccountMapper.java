package com.medcore.hms.user.mapper;

import com.medcore.hms.security.AuthenticatedUser;
import com.medcore.hms.user.domain.UserAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserAccountMapper {

    @Mapping(target = "userId", source = "id")
    @Mapping(
            target = "role",
            expression = "java(account.getRole() != null ? account.getRole().name() : null)"
    )
    @Mapping(
            target = "enabled",
            expression = "java(account.canAuthenticate(java.time.Instant.now()))"
    )
    @Mapping(
            target = "accountNonLocked",
            expression = "java(account.getStatus() != com.medcore.hms.user.domain.UserAccountStatus.LOCKED)"
    )
    AuthenticatedUser toAuthenticatedUser(UserAccount account);
}