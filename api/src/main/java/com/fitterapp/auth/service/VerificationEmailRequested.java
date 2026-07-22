package com.fitterapp.auth.service;

public record VerificationEmailRequested(
        String email,
        String fullName,
        String rawToken) {
}
