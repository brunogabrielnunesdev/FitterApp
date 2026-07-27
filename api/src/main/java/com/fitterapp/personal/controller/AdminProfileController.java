package com.fitterapp.personal.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitterapp.personal.dto.profile.ProfileActionResponseDto;
import com.fitterapp.personal.dto.profile.ProfileStatusResponseDto;
import com.fitterapp.personal.mapper.ProfileMapper;
import com.fitterapp.personal.service.query.ListProfilesForReviewService;
import com.fitterapp.personal.dto.review.RejectProfileRequestDto;
import com.fitterapp.personal.service.review.ApproveProfileCommand;
import com.fitterapp.personal.service.review.RejectProfileCommand;
import com.fitterapp.personal.service.review.ReviewProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/personal-profiles")
@RequiredArgsConstructor
public class AdminProfileController {

    private final ReviewProfileService reviewProfileService;
    private final ListProfilesForReviewService listProfilesForReviewService;
    private final ProfileMapper profileMapper;

    @org.springframework.web.bind.annotation.GetMapping("/pending-review")
    public ResponseEntity<java.util.List<ProfileStatusResponseDto>> listPendingReview() {
        var response = listProfilesForReviewService.listPending().stream()
                .map(profileMapper::toStatusResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{profileId}/approval")
    public ResponseEntity<ProfileActionResponseDto> approve(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID profileId) {
        var result = reviewProfileService.approve(new ApproveProfileCommand(adminUserId(jwt), profileId));
        return ResponseEntity.ok(new ProfileActionResponseDto(result.profileId(), result.revisionId()));
    }

    @PatchMapping("/{profileId}/rejection")
    public ResponseEntity<ProfileActionResponseDto> reject(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID profileId,
            @Valid @RequestBody RejectProfileRequestDto request) {
        var result = reviewProfileService.reject(new RejectProfileCommand(
                adminUserId(jwt),
                profileId,
                request.reason()));
        return ResponseEntity.ok(new ProfileActionResponseDto(result.profileId(), result.revisionId()));
    }

    private UUID adminUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
