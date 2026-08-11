package com.fitterapp.personal.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminCreatePersonalRequestDto(
    @NotBlank @Size(max = 120) String accountFullName,
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Pattern(regexp = "^\\+[1-9][0-9]{7,14}$") String phoneNumber,
    @NotBlank @Size(min = 8, max = 72) String temporaryPassword,
    @NotNull @Valid AdminPersonalProfileInputDto profile,
    @NotBlank @Size(max = 1500) String reason) {}
