package com.fitterapp.personal.service.revision;

import java.util.UUID;

public record StartProfileRevisionCommand(UUID userId, UUID profileId) {}
