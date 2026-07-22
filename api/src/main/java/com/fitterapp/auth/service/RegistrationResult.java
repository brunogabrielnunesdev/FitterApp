package com.fitterapp.auth.service;

import java.util.UUID;

public record RegistrationResult(
        UUID userId,
        String verificationToken) {
}
