package com.fitterapp.auth.service;

public record LoginResult(
        String accessToken,
        String refreshToken,
        long expiresInSeconds) {
}
