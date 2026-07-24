package com.fitterapp.auth.service.login;

public record LoginResult(
        String accessToken,
        String refreshToken,
        long expiresInSeconds) {
}
