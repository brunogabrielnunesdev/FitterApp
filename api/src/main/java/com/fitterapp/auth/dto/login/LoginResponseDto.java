package com.fitterapp.auth.dto.login;

public record LoginResponseDto(
        String tokenType,
        String accessToken,
        String refreshToken,
        long expiresInSeconds) {
}
