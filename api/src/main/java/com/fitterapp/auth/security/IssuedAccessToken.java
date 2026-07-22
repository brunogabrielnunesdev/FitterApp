package com.fitterapp.auth.security;

import java.time.OffsetDateTime;

public record IssuedAccessToken(
        String value,
        OffsetDateTime expiresAt) {
}
