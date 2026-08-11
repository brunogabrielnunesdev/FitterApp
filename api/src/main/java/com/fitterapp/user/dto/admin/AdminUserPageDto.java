package com.fitterapp.user.dto.admin;

import java.util.List;

public record AdminUserPageDto(
    List<AdminUserSummaryDto> content,
    int page,
    int size,
    long totalElements,
    int totalPages) {}
