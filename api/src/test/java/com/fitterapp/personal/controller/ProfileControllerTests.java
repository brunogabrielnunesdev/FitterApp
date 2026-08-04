package com.fitterapp.personal.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fitterapp.personal.dto.profile.*;
import com.fitterapp.personal.mapper.*;
import com.fitterapp.personal.service.create.*;
import com.fitterapp.personal.service.cref.*;
import com.fitterapp.personal.service.modality.*;
import com.fitterapp.personal.service.publication.*;
import com.fitterapp.personal.service.query.GetOwnProfileService;
import com.fitterapp.personal.service.query.GetOwnProfileDraftService;
import com.fitterapp.personal.service.revision.StartProfileRevisionService;
import com.fitterapp.personal.service.service.*;
import com.fitterapp.personal.service.submission.*;
import com.fitterapp.personal.service.update.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTests {
  @Mock CreateProfileService create;
  @Mock UpdateProfileDraftService update;
  @Mock UpsertCrefService cref;
  @Mock UpdateProfileModalitiesService modalities;
  @Mock UpdateProfileServiceModesService modes;
  @Mock UpdateProfileServiceAreasService areas;
  @Mock SubmitProfileForReviewService submission;
  @Mock ProfilePublicationService publication;
  @Mock GetOwnProfileService ownProfile;
  @Mock GetOwnProfileDraftService ownProfileDraft;
  @Mock StartProfileRevisionService startRevision;

  @Test
  void createsProfileForJwtSubject() {
    UUID userId = UUID.randomUUID(), profileId = UUID.randomUUID(), revisionId = UUID.randomUUID();
    when(create.create(any())).thenReturn(new CreateProfileResult(profileId, revisionId));
    var response = controller().create(jwt(userId), new CreateProfileRequestDto());
    assertThat(response.getStatusCode().value()).isEqualTo(201);
    assertThat(response.getBody()).isEqualTo(new ProfileActionResponseDto(profileId, revisionId));
    verify(create).create(new CreateProfileCommand(userId));
  }

  private ProfileController controller() {
    return new ProfileController(
        new ProfileMapper(),
        create,
        update,
        cref,
        modalities,
        modes,
        areas,
        submission,
        publication,
        ownProfile,
        ownProfileDraft,
        startRevision);
  }

  private Jwt jwt(UUID id) {
    return new Jwt(
        "token",
        Instant.now(),
        Instant.now().plusSeconds(60),
        Map.of("alg", "none"),
        Map.of("sub", id.toString()));
  }
}
