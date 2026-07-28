package com.fitterapp.personal.service.modality;

import java.util.List;
import java.util.UUID;

public record UpdateProfileModalitiesResult(
    UUID profileId, UUID revisionId, List<Short> modalityIds) {}
