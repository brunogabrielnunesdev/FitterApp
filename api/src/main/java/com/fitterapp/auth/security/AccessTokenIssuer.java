package com.fitterapp.auth.security;

import com.fitterapp.user.entity.RoleName;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public interface AccessTokenIssuer {

  IssuedAccessToken issue(UUID userId, String email, Set<RoleName> roles, OffsetDateTime issuedAt);
}
