package com.fitterapp.auth.dto.session;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDto(@NotBlank String refreshToken) {}
