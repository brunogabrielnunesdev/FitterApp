package com.fitterapp.personal.service.review;
import java.util.UUID;
public record ApproveProfileCommand(UUID adminUserId, UUID profileId) { }
