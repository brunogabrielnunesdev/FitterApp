package com.fitterapp.personal.controller;

import com.fitterapp.personal.dto.publicprofile.*;
import com.fitterapp.personal.entity.service.ServiceMode;
import com.fitterapp.personal.mapper.PublicProfileMapper;
import com.fitterapp.personal.service.contact.StartWhatsappContactCommand;
import com.fitterapp.personal.service.contact.StartWhatsappContactService;
import com.fitterapp.personal.service.publicprofile.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/personals")
@RequiredArgsConstructor
public class PublicProfileController {
  private final ListPublicProfilesService listService;
  private final GetPublicProfileService getService;
  private final StartWhatsappContactService contactService;
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
  public ResponseEntity<PublicProfileDetailDto> get(@PathVariable String slug) {
    return ResponseEntity.ok(mapper.toDetail(getService.get(slug)));
  }

  @PostMapping("/{slug}/contact/whatsapp")
  public ResponseEntity<WhatsappContactResponseDto> startWhatsappContact(
      @PathVariable String slug, @AuthenticationPrincipal Jwt jwt) {
    var result = contactService.start(new StartWhatsappContactCommand(slug, userId(jwt)));
    return ResponseEntity.ok(new WhatsappContactResponseDto(result.whatsappUrl()));
  }

  private UUID userId(Jwt jwt) {
    return jwt == null ? null : UUID.fromString(jwt.getSubject());
  }
}
