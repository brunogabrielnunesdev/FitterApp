package com.fitterapp.auth.dto.emailconfirm;

import jakarta.validation.constraints.NotBlank;

public record ConfirmEmailRequestDto(@NotBlank String token) {
}
