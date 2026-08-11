package com.fitterapp.personal.dto.admin;

import java.util.List;

public record AdminProfilePageDto(
    List<AdminProfileSummaryDto> content, int page, int size, long totalElements, int totalPages) {}
