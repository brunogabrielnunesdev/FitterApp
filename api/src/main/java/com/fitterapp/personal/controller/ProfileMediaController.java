package com.fitterapp.personal.controller;

import com.fitterapp.personal.service.media.ProfileMediaService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ProfileMediaController {
  private final ProfileMediaService media;

  @PostMapping(
      value = "/api/v1/me/personal-profile/{profileId}/photo",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Photo uploadPhoto(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID profileId,
      @RequestPart("file") MultipartFile file) {
    return new Photo(media.uploadPhoto(UUID.fromString(jwt.getSubject()), profileId, file));
  }

  @PostMapping(
      value = "/api/v1/me/personal-profile/{profileId}/cref/document",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Photo uploadCref(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID profileId,
      @RequestParam String registrationCode,
      @RequestPart("file") MultipartFile file) {
    return new Photo(
        media.uploadCref(UUID.fromString(jwt.getSubject()), profileId, registrationCode, file));
  }

  @GetMapping(
      value = "/api/v1/public/media/personals/{profileId}/{imageId}.webp",
      produces = "image/webp")
  public ResponseEntity<byte[]> photo(@PathVariable UUID profileId, @PathVariable UUID imageId) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noCache())
        .contentType(MediaType.parseMediaType("image/webp"))
        .body(media.publicPhoto(profileId, imageId));
  }

  @GetMapping(value = "/api/v1/admin/personal-profiles/{profileId}/photo", produces = "image/webp")
  public ResponseEntity<byte[]> adminPhoto(@PathVariable UUID profileId) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .contentType(MediaType.parseMediaType("image/webp"))
        .body(media.adminPhoto(profileId));
  }

  @GetMapping(
      value = "/api/v1/admin/personal-profiles/{profileId}/cref/document",
      produces = "image/webp")
  public ResponseEntity<byte[]> cref(@PathVariable UUID profileId) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .contentType(MediaType.parseMediaType("image/webp"))
        .body(media.adminCref(profileId));
  }

  public record Photo(String key) {}
}
