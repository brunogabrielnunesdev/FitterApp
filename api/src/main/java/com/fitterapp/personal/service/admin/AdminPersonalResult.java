package com.fitterapp.personal.service.admin;

import java.util.UUID;

public record AdminPersonalResult(UUID userId, UUID profileId, UUID revisionId) {}
