package com.fitterapp.personal.dto.admin;

import java.util.UUID;

public record AdminPersonalActionResponseDto(UUID userId, UUID profileId, UUID revisionId) {}
