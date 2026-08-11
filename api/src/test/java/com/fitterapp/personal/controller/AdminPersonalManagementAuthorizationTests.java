package com.fitterapp.personal.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitterapp.auth.security.SecurityConfig;
import com.fitterapp.personal.mapper.AdminPersonalManagementMapper;
import com.fitterapp.personal.service.admin.AdminCreatePersonalCommand;
import com.fitterapp.personal.service.admin.AdminPersonalManagementService;
import com.fitterapp.personal.service.admin.AdminPersonalResult;
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

@WebMvcTest(AdminPersonalManagementController.class)
@Import(SecurityConfig.class)
class AdminPersonalManagementAuthorizationTests {
  @Autowired private MockMvc mockMvc;

  @MockitoBean private AdminPersonalManagementService service;
  @MockitoBean private AdminPersonalManagementMapper mapper;
  @MockitoBean private JwtDecoder jwtDecoder;

  @Test
  void rejectsAnonymousCreation() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/admin/personal-profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCreateBody(null)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void rejectsPersonalEditingAnotherProfile() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/admin/personal-profiles/{profileId}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUpdateBody(null))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PERSONAL"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void allowsAdministratorToCreateAccountAndProfile() throws Exception {
    UUID adminId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID profileId = UUID.randomUUID();
    UUID revisionId = UUID.randomUUID();
    when(mapper.toCreateCommand(any(), any()))
        .thenReturn(org.mockito.Mockito.mock(AdminCreatePersonalCommand.class));
    when(service.create(any()))
        .thenReturn(new AdminPersonalResult(userId, profileId, revisionId));

    mockMvc
        .perform(
            post("/api/v1/admin/personal-profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCreateBody(null))
                .with(
                    jwt()
                        .jwt(builder -> builder.subject(adminId.toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isCreated())
        .andExpect(
            header()
                .string(
                    "Location", "/api/v1/admin/personal-profiles/" + profileId));
  }

  @Test
  void rejectsInvalidAccountData() throws Exception {
    String body = validCreateBody(null).replace("new@example.com", "invalid-email");

    mockMvc
        .perform(
            post("/api/v1/admin/personal-profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void rejectsPartialCref() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/admin/personal-profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCreateBody("\"cref\":{\"registrationCode\":\"123-G/PR\"},"))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isBadRequest());
  }

  private String validCreateBody(String cref) {
    return """
        {
          "accountFullName":"New Personal",
          "email":"new@example.com",
          "phoneNumber":"+5544999999999",
          "temporaryPassword":"temporary-password",
          "profile":%s,
          "reason":"Cadastro administrativo"
        }
        """
        .formatted(profileBody(cref));
  }

  private String validUpdateBody(String cref) {
    return """
        {"profile":%s,"reason":"Atualização administrativa"}
        """
        .formatted(profileBody(cref));
  }

  private String profileBody(String cref) {
    return """
        {
          "fullName":"Professional Name",
          "biography":"Biography",
          "whatsapp":"+5544999999999",
          %s
          "modalityIds":[],
          "serviceModes":[],
          "serviceAreas":[]
        }
        """
        .formatted(cref == null ? "" : cref);
  }
}
