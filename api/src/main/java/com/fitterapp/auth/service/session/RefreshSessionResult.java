package com.fitterapp.auth.service.session;

public record RefreshSessionResult(
    String accessToken, String refreshToken, long expiresInSeconds) {}
