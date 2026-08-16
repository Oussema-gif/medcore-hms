package com.medcore.hms.security;

import java.util.Optional;

public interface UserSecurityService {

    Optional<AuthenticatedUser> loadById(Long userId);
}