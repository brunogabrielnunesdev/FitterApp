package com.fitterapp.personal.service.review;

import java.util.UUID;

public record RejectProfileCommand(UUID adminUserId, UUID profileId, String reason) {}
