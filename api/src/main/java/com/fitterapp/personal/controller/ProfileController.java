package com.fitterapp.personal.controller;

import com.fitterapp.personal.dto.profile.CreateProfileRequestDto;
import com.fitterapp.personal.dto.profile.ProfileActionResponseDto;
import com.fitterapp.personal.dto.profile.ProfileDraftResponseDto;
import com.fitterapp.personal.dto.profile.ProfileStatusResponseDto;
import com.fitterapp.personal.dto.profile.UpdateModalitiesRequestDto;
import com.fitterapp.personal.dto.profile.UpdateProfileDraftRequestDto;
import com.fitterapp.personal.dto.profile.UpdateServiceAreasRequestDto;
import com.fitterapp.personal.dto.profile.UpdateServiceModesRequestDto;
import com.fitterapp.personal.dto.profile.UpsertCrefRequestDto;
import com.fitterapp.personal.mapper.ProfileMapper;
import com.fitterapp.personal.service.create.CreateProfileService;
import com.fitterapp.personal.service.cref.UpsertCrefService;
import com.fitterapp.personal.service.modality.UpdateProfileModalitiesService;
import com.fitterapp.personal.service.publication.ProfilePublicationService;
import com.fitterapp.personal.service.publication.PublishProfileCommand;
import com.fitterapp.personal.service.publication.UnpublishProfileCommand;
import com.fitterapp.personal.service.query.GetOwnProfileDraftService;
import com.fitterapp.personal.service.query.GetOwnProfileService;
import com.fitterapp.personal.service.revision.StartProfileRevisionCommand;
import com.fitterapp.personal.service.revision.StartProfileRevisionService;
import com.fitterapp.personal.service.service.UpdateProfileServiceAreasService;
import com.fitterapp.personal.service.service.UpdateProfileServiceModesService;
import com.fitterapp.personal.service.submission.SubmitProfileForReviewCommand;
import com.fitterapp.personal.service.submission.SubmitProfileForReviewService;
import com.fitterapp.personal.service.update.UpdateProfileDraftService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/personal-profile")
@RequiredArgsConstructor
public class ProfileController {

  private final ProfileMapper mapper;
  private final CreateProfileService createProfileService;
  private final UpdateProfileDraftService updateProfileDraftService;
  private final UpsertCrefService upsertCrefService;
  private final UpdateProfileModalitiesService updateProfileModalitiesService;
  private final UpdateProfileServiceModesService updateProfileServiceModesService;
  private final UpdateProfileServiceAreasService updateProfileServiceAreasService;
  private final SubmitProfileForReviewService submitProfileForReviewService;
  private final ProfilePublicationService profilePublicationService;
  private final GetOwnProfileService getOwnProfileService;
  private final GetOwnProfileDraftService getOwnProfileDraftService;
  private final StartProfileRevisionService startProfileRevisionService;

  @GetMapping
  public ResponseEntity<ProfileStatusResponseDto> getOwnProfile(@AuthenticationPrincipal Jwt jwt) {
    var profile = getOwnProfileService.get(userId(jwt));
    return ResponseEntity.ok(mapper.toStatusResponse(profile));
  }

  @GetMapping("/draft")
  public ResponseEntity<ProfileDraftResponseDto> getOwnProfileDraft(
      @AuthenticationPrincipal Jwt jwt) {
    return ResponseEntity.ok(getOwnProfileDraftService.get(userId(jwt)));
  }

  @PostMapping
  public ResponseEntity<ProfileActionResponseDto> create(
      @AuthenticationPrincipal Jwt jwt, @RequestBody CreateProfileRequestDto request) {
    var result = createProfileService.create(mapper.toCreateCommand(userId(jwt), request));
    var response = new ProfileActionResponseDto(result.profileId(), result.revisionId());
    URI location = URI.create("/api/v1/me/personal-profile/" + result.profileId());
    return ResponseEntity.created(location).body(response);
  }

  @PutMapping("/{profileId}")
  public ResponseEntity<Void> updateDraft(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID profileId,
      @RequestBody UpdateProfileDraftRequestDto request) {
    updateProfileDraftService.update(mapper.toUpdateCommand(userId(jwt), profileId, request));
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{profileId}/cref")
  public ResponseEntity<Void> updateCref(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID profileId,
      @Valid @RequestBody UpsertCrefRequestDto request) {
    upsertCrefService.upsert(mapper.toCrefCommand(userId(jwt), profileId, request));
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{profileId}/modalities")
  public ResponseEntity<Void> updateModalities(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID profileId,
      @RequestBody UpdateModalitiesRequestDto request) {
    updateProfileModalitiesService.update(
        mapper.toModalitiesCommand(userId(jwt), profileId, request));
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{profileId}/service-modes")
  public ResponseEntity<Void> updateServiceModes(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID profileId,
      @RequestBody UpdateServiceModesRequestDto request) {
    updateProfileServiceModesService.update(
        mapper.toServiceModesCommand(userId(jwt), profileId, request));
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{profileId}/service-areas")
  public ResponseEntity<Void> updateServiceAreas(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID profileId,
      @RequestBody UpdateServiceAreasRequestDto request) {
    updateProfileServiceAreasService.update(
        mapper.toServiceAreasCommand(userId(jwt), profileId, request));
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{profileId}/submission")
  public ResponseEntity<Void> submit(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID profileId) {
    submitProfileForReviewService.submit(new SubmitProfileForReviewCommand(userId(jwt), profileId));
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{profileId}/revisions")
  public ResponseEntity<ProfileActionResponseDto> startRevision(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID profileId) {
    var result =
        startProfileRevisionService.start(
            new StartProfileRevisionCommand(userId(jwt), profileId));
    return ResponseEntity.ok(
        new ProfileActionResponseDto(result.profileId(), result.revisionId()));
  }

  @PostMapping("/{profileId}/publication")
  public ResponseEntity<Void> publish(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID profileId) {
    profilePublicationService.publish(new PublishProfileCommand(userId(jwt), profileId));
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{profileId}/publication")
  public ResponseEntity<Void> unpublish(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID profileId) {
    profilePublicationService.unpublish(new UnpublishProfileCommand(userId(jwt), profileId));
    return ResponseEntity.noContent().build();
  }

  private UUID userId(Jwt jwt) {
    return UUID.fromString(jwt.getSubject());
  }
}
