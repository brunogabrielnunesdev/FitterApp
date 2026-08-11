package com.fitterapp.moderation.service.suspension;

import java.util.UUID;

public record SuspendProfileCommand(UUID adminUserId, UUID profileId, String reason) {}
