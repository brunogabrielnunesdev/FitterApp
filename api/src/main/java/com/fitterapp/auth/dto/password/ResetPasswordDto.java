package com.fitterapp.auth.dto.password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordDto(
    @NotBlank String token, @NotBlank @Size(min = 8, max = 72) String newPassword) {}
