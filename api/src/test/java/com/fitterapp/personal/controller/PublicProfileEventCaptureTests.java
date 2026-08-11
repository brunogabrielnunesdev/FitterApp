package com.fitterapp.personal.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitterapp.analytics.entity.event.EventSource;
import com.fitterapp.analytics.service.PublicCatalogEventService;
import com.fitterapp.auth.security.SecurityConfig;
import com.fitterapp.personal.dto.publicprofile.PublicProfileDetailDto;
import com.fitterapp.personal.dto.publicprofile.PublicProfilePageDto;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.mapper.PublicProfileMapper;
import com.fitterapp.personal.service.contact.StartWhatsappContactCommand;
import com.fitterapp.personal.service.contact.StartWhatsappContactResult;
import com.fitterapp.personal.service.contact.StartWhatsappContactService;
import com.fitterapp.personal.service.publicprofile.GetPublicProfileService;
import com.fitterapp.personal.service.publicprofile.ListPublicProfilesService;
import com.fitterapp.personal.service.publicprofile.PublicProfileDetails;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PublicProfileController.class)
@Import(SecurityConfig.class)
class PublicProfileEventCaptureTests {
  @Autowired private MockMvc mockMvc;

  @MockitoBean private ListPublicProfilesService listService;
  @MockitoBean private GetPublicProfileService getService;
  @MockitoBean private StartWhatsappContactService contactService;
  @MockitoBean private PublicCatalogEventService eventService;
  @MockitoBean private PublicProfileMapper mapper;
  @MockitoBean private JwtDecoder jwtDecoder;

  @Test
  void recordsAnonymousSearchAsPublicWebByDefault() throws Exception {
    when(listService.list(eq("Bruno"), eq((short) 2), eq(null), eq(null), any()))
        .thenReturn(org.springframework.data.domain.Page.empty());
    when(mapper.toPage(any())).thenReturn(mock(PublicProfilePageDto.class));

    mockMvc
        .perform(
            get("/api/v1/public/personals?query=Bruno&modalityId=2")
                .header("X-Visitor-Id", "visitor-1")
                .header("X-Idempotency-Key", "request-1"))
        .andExpect(status().isOk());

    verify(eventService)
        .recordSearch(
            null,
            EventSource.PUBLIC_WEB,
            "Bruno",
            (short) 2,
            null,
            null,
            0,
            20,
            0,
            "visitor-1",
            "request-1");
  }

  @Test
  void recordsAuthenticatedViewWithClientSource() throws Exception {
    UUID userId = UUID.randomUUID();
    Profile profile = mock(Profile.class);
    PublicProfileDetails details = mock(PublicProfileDetails.class);
    when(details.profile()).thenReturn(profile);
    when(getService.get("bruno-personal")).thenReturn(details);
    when(mapper.toDetail(details)).thenReturn(mock(PublicProfileDetailDto.class));

    mockMvc
        .perform(
            get("/api/v1/public/personals/bruno-personal?source=MOBILE_APP")
                .with(
                    jwt()
                        .jwt(builder -> builder.subject(userId.toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
        .andExpect(status().isOk());

    verify(eventService)
        .recordPersonalView(userId, EventSource.MOBILE_APP, profile, null, null);
  }

  @Test
  void recordsWhatsappContactAsPublicWebByDefault() throws Exception {
    when(contactService.start(any()))
        .thenReturn(new StartWhatsappContactResult("https://wa.me/5544999999999"));

    mockMvc
        .perform(post("/api/v1/public/personals/bruno-personal/contact/whatsapp"))
        .andExpect(status().isOk());

    verify(contactService)
        .start(
            new StartWhatsappContactCommand(
                "bruno-personal", null, EventSource.PUBLIC_WEB, null, null));
  }
}
