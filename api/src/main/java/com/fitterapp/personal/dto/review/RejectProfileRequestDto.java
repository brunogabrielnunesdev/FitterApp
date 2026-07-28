package com.fitterapp.personal.dto.review;

import jakarta.validation.constraints.NotBlank;

public record RejectProfileRequestDto(@NotBlank String reason) {}
