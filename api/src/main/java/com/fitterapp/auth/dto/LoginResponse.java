package com.fitterapp.auth.dto;

public record LoginResponse(
        String tokenType,
        String accessToken,
        String refreshToken,
        long expiresInSeconds) {
}
