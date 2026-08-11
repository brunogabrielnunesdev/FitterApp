package com.fitterapp.moderation.service.suspension;

import java.util.UUID;

public record ReactivateProfileCommand(UUID adminUserId, UUID profileId, String reason) {}
