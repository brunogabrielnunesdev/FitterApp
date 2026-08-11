package com.fitterapp.personal.service.admin;

import java.util.UUID;

public record AdminUpdatePersonalCommand(
    UUID adminUserId, UUID profileId, AdminPersonalInput profile, String reason) {}
