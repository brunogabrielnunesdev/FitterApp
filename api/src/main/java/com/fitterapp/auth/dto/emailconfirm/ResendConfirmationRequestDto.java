package com.fitterapp.auth.dto.emailconfirm;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResendConfirmationRequestDto(
        @NotBlank @Email @Size(max = 254) String email) {
}
