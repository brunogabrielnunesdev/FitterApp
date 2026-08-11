package com.fitterapp.moderation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileModerationRequestDto(@NotBlank @Size(max = 1500) String reason) {}
