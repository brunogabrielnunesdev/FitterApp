package com.fitterapp.personal.dto.admin;

import com.fitterapp.user.entity.UserStatus;
import java.util.UUID;

public record AdminAccountDto(
    UUID userId, String fullName, String email, String phoneNumber, UserStatus status) {}
