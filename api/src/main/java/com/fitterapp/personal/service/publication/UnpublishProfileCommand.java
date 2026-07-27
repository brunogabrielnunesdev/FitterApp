package com.fitterapp.personal.service.publication;
import java.util.UUID;
public record UnpublishProfileCommand(UUID userId, UUID profileId) { }
