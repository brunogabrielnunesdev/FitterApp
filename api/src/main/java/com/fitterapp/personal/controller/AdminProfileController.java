package com.fitterapp.personal.controller;

import com.fitterapp.personal.dto.admin.AdminProfileDetailDto;
import com.fitterapp.personal.dto.admin.AdminProfilePageDto;
import com.fitterapp.personal.dto.profile.ProfileActionResponseDto;
import com.fitterapp.personal.dto.profile.ProfileStatusResponseDto;
import com.fitterapp.personal.dto.review.RejectProfileRequestDto;
import com.fitterapp.personal.entity.profile.ProfileStatus;
import com.fitterapp.personal.mapper.AdminProfileMapper;
import com.fitterapp.personal.mapper.ProfileMapper;
import com.fitterapp.personal.service.query.GetAdminProfileService;
import com.fitterapp.personal.service.query.ListAdminProfilesService;
import com.fitterapp.personal.service.query.ListProfilesForReviewService;
import com.fitterapp.personal.service.review.ApproveProfileCommand;
import com.fitterapp.personal.service.review.RejectProfileCommand;
import com.fitterapp.personal.service.review.ReviewProfileService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/personal-profiles")
@RequiredArgsConstructor
public class AdminProfileController {

  private final ReviewProfileService reviewProfileService;
  private final ListProfilesForReviewService listProfilesForReviewService;
  private final ListAdminProfilesService listAdminProfilesService;
  private final GetAdminProfileService getAdminProfileService;
  private final ProfileMapper profileMapper;
  private final AdminProfileMapper adminProfileMapper;

  @GetMapping
  public ResponseEntity<AdminProfilePageDto> list(
      @RequestParam(required = false) ProfileStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 100);
    var pageable =
        PageRequest.of(
            safePage, safeSize, Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.asc("id")));
    return ResponseEntity.ok(
        adminProfileMapper.toPage(listAdminProfilesService.list(status, pageable)));
  }

  @GetMapping("/{profileId}")
  public ResponseEntity<AdminProfileDetailDto> get(@PathVariable UUID profileId) {
    return ResponseEntity.ok(adminProfileMapper.toDetail(getAdminProfileService.get(profileId)));
  }

  @org.springframework.web.bind.annotation.GetMapping("/pending-review")
  public ResponseEntity<java.util.List<ProfileStatusResponseDto>> listPendingReview() {
    var response =
        listProfilesForReviewService.listPending().stream()
            .map(profileMapper::toStatusResponse)
            .toList();
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{profileId}/approval")
  public ResponseEntity<ProfileActionResponseDto> approve(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID profileId) {
    var result =
        reviewProfileService.approve(new ApproveProfileCommand(adminUserId(jwt), profileId));
    return ResponseEntity.ok(new ProfileActionResponseDto(result.profileId(), result.revisionId()));
  }

  @PatchMapping("/{profileId}/rejection")
  public ResponseEntity<ProfileActionResponseDto> reject(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID profileId,
      @Valid @RequestBody RejectProfileRequestDto request) {
    var result =
        reviewProfileService.reject(
            new RejectProfileCommand(adminUserId(jwt), profileId, request.reason()));
    return ResponseEntity.ok(new ProfileActionResponseDto(result.profileId(), result.revisionId()));
  }

  private UUID adminUserId(Jwt jwt) {
    return UUID.fromString(jwt.getSubject());
  }
}
