package com.fitterapp.personal.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitterapp.moderation.dto.ProfileModerationRequestDto;
import com.fitterapp.moderation.service.suspension.ProfileModerationResult;
import com.fitterapp.moderation.service.suspension.ProfileModerationService;
import com.fitterapp.moderation.service.suspension.ReactivateProfileCommand;
import com.fitterapp.moderation.service.suspension.SuspendProfileCommand;
import com.fitterapp.personal.dto.admin.AdminProfileDetailDto;
import com.fitterapp.personal.dto.admin.AdminProfilePageDto;
import com.fitterapp.personal.dto.review.RejectProfileRequestDto;
import com.fitterapp.personal.entity.profile.ProfileStatus;
import com.fitterapp.personal.mapper.AdminProfileMapper;
import com.fitterapp.personal.mapper.ProfileMapper;
import com.fitterapp.personal.service.query.AdminProfileDetails;
import com.fitterapp.personal.service.query.GetAdminProfileService;
import com.fitterapp.personal.service.query.ListAdminProfilesService;
import com.fitterapp.personal.service.query.ListProfilesForReviewService;
import com.fitterapp.personal.service.review.RejectProfileCommand;
import com.fitterapp.personal.service.review.ReviewProfileResult;
import com.fitterapp.personal.service.review.ReviewProfileService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class AdminProfileControllerTests {

  @Mock private ReviewProfileService review;
  @Mock private ProfileModerationService moderation;
  @Mock private ListProfilesForReviewService pending;
  @Mock private ListAdminProfilesService listAdmin;
  @Mock private GetAdminProfileService getAdmin;
  @Mock private AdminProfileMapper adminMapper;

  @Test
  void listsProfilesWithSafePaginationAndStableSorting() {
    var pageResponse = new AdminProfilePageDto(List.of(), 0, 100, 0, 0);
    when(listAdmin.list(eq(ProfileStatus.PENDING_REVIEW), any())).thenReturn(Page.empty());
    when(adminMapper.toPage(any())).thenReturn(pageResponse);

    var response = controller().list(ProfileStatus.PENDING_REVIEW, -3, 500);

    assertThat(response.getBody()).isSameAs(pageResponse);
    ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(listAdmin).list(eq(ProfileStatus.PENDING_REVIEW), pageable.capture());
    assertThat(pageable.getValue().getPageNumber()).isZero();
    assertThat(pageable.getValue().getPageSize()).isEqualTo(100);
    assertThat(pageable.getValue().getSort().getOrderFor("updatedAt").isDescending()).isTrue();
    assertThat(pageable.getValue().getSort().getOrderFor("id").isAscending()).isTrue();
  }

  @Test
  void getsTheCompleteAdministrativeProfile() {
    UUID profileId = UUID.randomUUID();
    AdminProfileDetails details = mock(AdminProfileDetails.class);
    AdminProfileDetailDto detail = mock(AdminProfileDetailDto.class);
    when(getAdmin.get(profileId)).thenReturn(details);
    when(adminMapper.toDetail(details)).thenReturn(detail);

    assertThat(controller().get(profileId).getBody()).isSameAs(detail);
  }

  @Test
  void rejectsUsingAdminJwtSubject() {
    UUID admin = UUID.randomUUID();
    UUID profile = UUID.randomUUID();
    UUID revision = UUID.randomUUID();
    when(review.reject(any())).thenReturn(new ReviewProfileResult(profile, revision));

    var response =
        controller().reject(jwt(admin), profile, new RejectProfileRequestDto("CREF inválido"));

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    verify(review).reject(new RejectProfileCommand(admin, profile, "CREF inválido"));
  }

  @Test
  void suspendsUsingAdminJwtSubjectAndReason() {
    UUID admin = UUID.randomUUID();
    UUID profile = UUID.randomUUID();
    UUID suspension = UUID.randomUUID();
    when(moderation.suspend(any()))
        .thenReturn(
            new ProfileModerationResult(
                profile,
                suspension,
                ProfileStatus.SUSPENDED,
                java.time.OffsetDateTime.parse("2026-08-11T12:00:00Z")));

    var response =
        controller()
            .suspend(jwt(admin), profile, new ProfileModerationRequestDto("Dados inconsistentes"));

    assertThat(response.getBody().suspensionId()).isEqualTo(suspension);
    verify(moderation).suspend(new SuspendProfileCommand(admin, profile, "Dados inconsistentes"));
  }

  @Test
  void reactivatesUsingAdminJwtSubjectAndReason() {
    UUID admin = UUID.randomUUID();
    UUID profile = UUID.randomUUID();
    UUID suspension = UUID.randomUUID();
    when(moderation.reactivate(any()))
        .thenReturn(
            new ProfileModerationResult(
                profile,
                suspension,
                ProfileStatus.PUBLISHED,
                java.time.OffsetDateTime.parse("2026-08-11T12:00:00Z")));

    controller()
        .reactivate(jwt(admin), profile, new ProfileModerationRequestDto("Correção validada"));

    verify(moderation)
        .reactivate(new ReactivateProfileCommand(admin, profile, "Correção validada"));
  }

  private AdminProfileController controller() {
    return new AdminProfileController(
        review, moderation, pending, listAdmin, getAdmin, new ProfileMapper(), adminMapper);
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
