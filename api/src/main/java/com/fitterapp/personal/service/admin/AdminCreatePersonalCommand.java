package com.fitterapp.personal.service.admin;

import java.util.UUID;

public record AdminCreatePersonalCommand(
    UUID adminUserId,
    String accountFullName,
    String email,
    String phoneNumber,
    String temporaryPassword,
    AdminPersonalInput profile,
    String reason) {}
