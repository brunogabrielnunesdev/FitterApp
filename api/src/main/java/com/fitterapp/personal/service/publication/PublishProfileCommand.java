package com.fitterapp.personal.service.publication;

import java.util.UUID;

public record PublishProfileCommand(UUID userId, UUID profileId) {}
