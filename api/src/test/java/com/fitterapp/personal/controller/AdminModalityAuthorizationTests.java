package com.fitterapp.personal.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitterapp.auth.security.SecurityConfig;
import com.fitterapp.personal.entity.modality.Modality;
import com.fitterapp.personal.service.modality.AdminModalityService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminModalityController.class)
@Import(SecurityConfig.class)
class AdminModalityAuthorizationTests {
  @Autowired private MockMvc mockMvc;

  @MockitoBean private AdminModalityService service;
  @MockitoBean private JwtDecoder jwtDecoder;

  @Test
  void rejectsAnonymousListing() throws Exception {
    mockMvc.perform(get("/api/v1/admin/modalities")).andExpect(status().isUnauthorized());
  }

  @Test
  void rejectsPersonalCreatingModality() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/admin/modalities")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Pilates\"}")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PERSONAL"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void rejectsStudentListingModalities() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/admin/modalities")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void allowsAdministratorToListInactiveModalities() throws Exception {
    Modality modality = modality((short) 9, "Pilates", "pilates", false);
    when(service.list()).thenReturn(List.of(modality));

    mockMvc
        .perform(
            get("/api/v1/admin/modalities")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].active").value(false));
  }

  @Test
  void allowsOwnerToCreateModality() throws Exception {
    Modality modality = modality((short) 9, "Pilates", "pilates", true);
    when(service.create("Pilates")).thenReturn(modality);

    mockMvc
        .perform(
            post("/api/v1/admin/modalities")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Pilates\"}")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OWNER"))))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/v1/admin/modalities/9"))
        .andExpect(jsonPath("$.slug").value("pilates"));
  }

  @Test
  void rejectsBlankName() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/admin/modalities")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\" \"}")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void validatesActivationFlag() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/admin/modalities/9/activation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isBadRequest());
  }

  private Modality modality(Short id, String name, String slug, boolean active) {
    Modality modality = mock(Modality.class);
    when(modality.getId()).thenReturn(id);
    when(modality.getName()).thenReturn(name);
    when(modality.getSlug()).thenReturn(slug);
    when(modality.isActive()).thenReturn(active);
    return modality;
  }
}
