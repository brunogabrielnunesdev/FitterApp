package com.fitterapp.personal.dto.publicprofile;

import java.util.List;

public record PublicProfilePageDto(
    List<PublicProfileCardDto> content, int page, int size, long totalElements, int totalPages) {}
