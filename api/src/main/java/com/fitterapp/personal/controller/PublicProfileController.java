package com.fitterapp.personal.controller;

import com.fitterapp.personal.dto.publicprofile.*;
import com.fitterapp.personal.entity.service.ServiceMode;
import com.fitterapp.personal.mapper.PublicProfileMapper;
import com.fitterapp.personal.service.publicprofile.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/personals")
@RequiredArgsConstructor
public class PublicProfileController {
  private final ListPublicProfilesService listService;
  private final GetPublicProfileService getService;
  private final PublicProfileMapper mapper;

  @GetMapping
  public ResponseEntity<PublicProfilePageDto> list(
      @RequestParam(required = false) String query,
      @RequestParam(required = false) Short modalityId,
      @RequestParam(required = false) String neighborhood,
      @RequestParam(required = false) ServiceMode serviceMode,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    int safePage = Math.max(page, 0), safeSize = Math.min(Math.max(size, 1), 50);
    var result =
        listService.list(
            query,
            modalityId,
            neighborhood,
            serviceMode,
            PageRequest.of(safePage, safeSize, Sort.unsorted()));
    return ResponseEntity.ok(mapper.toPage(result));
  }

  @GetMapping("/{slug}")
  public ResponseEntity<PublicProfileCardDto> get(@PathVariable String slug) {
    return ResponseEntity.ok(mapper.toCard(getService.get(slug)));
  }
}
