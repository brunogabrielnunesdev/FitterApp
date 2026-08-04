package com.fitterapp.auth.dto.password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequestPasswordResetDto(
    @NotBlank @Email @Size(max = 254) String email) {}
