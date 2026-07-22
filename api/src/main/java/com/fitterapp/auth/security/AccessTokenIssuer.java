package com.fitterapp.auth.security;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import com.fitterapp.user.entity.RoleName;

public interface AccessTokenIssuer {

    IssuedAccessToken issue(
            UUID userId,
            String email,
            Set<RoleName> roles,
            OffsetDateTime issuedAt);
}
