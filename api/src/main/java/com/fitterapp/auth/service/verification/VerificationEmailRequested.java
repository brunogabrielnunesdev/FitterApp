package com.fitterapp.auth.service.verification;

public record VerificationEmailRequested(String email, String fullName, String rawToken) {}
