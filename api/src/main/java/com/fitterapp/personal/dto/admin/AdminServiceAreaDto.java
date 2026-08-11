package com.fitterapp.personal.dto.admin;

import java.util.UUID;

public record AdminServiceAreaDto(
    UUID id, String city, String stateCode, String neighborhood, String description) {}
