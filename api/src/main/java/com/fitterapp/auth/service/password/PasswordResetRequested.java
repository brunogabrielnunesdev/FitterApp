package com.fitterapp.auth.service.password;

public record PasswordResetRequested(String email, String fullName, String rawToken) {}
