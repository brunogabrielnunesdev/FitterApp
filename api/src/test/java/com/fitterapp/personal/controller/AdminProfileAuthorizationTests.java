package com.fitterapp.personal.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitterapp.auth.security.SecurityConfig;
import com.fitterapp.moderation.service.suspension.ProfileModerationResult;
import com.fitterapp.moderation.service.suspension.ProfileModerationService;
import com.fitterapp.personal.dto.admin.AdminProfileDetailDto;
import com.fitterapp.personal.dto.admin.AdminProfilePageDto;
import com.fitterapp.personal.entity.profile.ProfileStatus;
import com.fitterapp.personal.mapper.AdminProfileMapper;
import com.fitterapp.personal.mapper.ProfileMapper;
import com.fitterapp.personal.service.query.AdminProfileDetails;
import com.fitterapp.personal.service.query.GetAdminProfileService;
import com.fitterapp.personal.service.query.ListAdminProfilesService;
import com.fitterapp.personal.service.query.ListProfilesForReviewService;
import com.fitterapp.personal.service.review.ReviewProfileService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminProfileController.class)
@Import(SecurityConfig.class)
class AdminProfileAuthorizationTests {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ReviewProfileService reviewProfileService;
  @MockitoBean private ProfileModerationService profileModerationService;
  @MockitoBean private ListProfilesForReviewService listProfilesForReviewService;
  @MockitoBean private ListAdminProfilesService listAdminProfilesService;
  @MockitoBean private GetAdminProfileService getAdminProfileService;
  @MockitoBean private ProfileMapper profileMapper;
  @MockitoBean private AdminProfileMapper adminProfileMapper;
  @MockitoBean private JwtDecoder jwtDecoder;

  @Test
  void rejectsAnonymousAccess() throws Exception {
    mockMvc.perform(get("/api/v1/admin/personal-profiles")).andExpect(status().isUnauthorized());
  }

  @Test
  void rejectsAProfessionalAccount() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/admin/personal-profiles")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PERSONAL"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void allowsAnAdministratorToListProfiles() throws Exception {
    when(listAdminProfilesService.list(any(), any()))
        .thenReturn(org.springframework.data.domain.Page.empty());
    when(adminProfileMapper.toPage(any()))
        .thenReturn(new AdminProfilePageDto(List.of(), 0, 20, 0, 0));

    mockMvc
        .perform(
            get("/api/v1/admin/personal-profiles")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk());
  }

  @Test
  void allowsAnOwnerToOpenProfileDetails() throws Exception {
    UUID profileId = UUID.randomUUID();
    AdminProfileDetails details = mock(AdminProfileDetails.class);
    AdminProfileDetailDto response = mock(AdminProfileDetailDto.class);
    when(getAdminProfileService.get(profileId)).thenReturn(details);
    when(adminProfileMapper.toDetail(details)).thenReturn(response);

    mockMvc
        .perform(
            get("/api/v1/admin/personal-profiles/{profileId}", profileId)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OWNER"))))
        .andExpect(status().isOk());
  }

  @Test
  void rejectsAProfessionalTryingToSuspendAProfile() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/admin/personal-profiles/{profileId}/suspension", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Irregularidade\"}")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PERSONAL"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void allowsAnAdministratorToSuspendAProfile() throws Exception {
    UUID adminId = UUID.randomUUID();
    UUID profileId = UUID.randomUUID();
    when(profileModerationService.suspend(any()))
        .thenReturn(
            new ProfileModerationResult(
                profileId,
                UUID.randomUUID(),
                ProfileStatus.SUSPENDED,
                java.time.OffsetDateTime.parse("2026-08-11T15:00:00Z")));

    mockMvc
        .perform(
            patch("/api/v1/admin/personal-profiles/{profileId}/suspension", profileId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Irregularidade\"}")
                .with(
                    jwt()
                        .jwt(builder -> builder.subject(adminId.toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk());
  }

  @Test
  void rejectsBlankReactivationReason() throws Exception {
    UUID adminId = UUID.randomUUID();
    mockMvc
        .perform(
            patch("/api/v1/admin/personal-profiles/{profileId}/reactivation", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\" \"}")
                .with(
                    jwt()
                        .jwt(builder -> builder.subject(adminId.toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isBadRequest());
  }
}
