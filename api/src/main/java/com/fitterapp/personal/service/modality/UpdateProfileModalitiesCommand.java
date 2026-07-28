package com.fitterapp.personal.service.modality;

import java.util.List;
import java.util.UUID;

public record UpdateProfileModalitiesCommand(
    UUID userId, UUID profileId, List<Short> modalityIds) {}
