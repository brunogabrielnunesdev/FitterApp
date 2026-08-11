package com.fitterapp.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitterapp.auth.security.SecurityConfig;
import com.fitterapp.user.dto.admin.AdminUserDetailDto;
import com.fitterapp.user.dto.admin.AdminUserPageDto;
import com.fitterapp.user.exception.UserNotFoundException;
import com.fitterapp.user.mapper.AdminUserMapper;
import com.fitterapp.user.service.query.AdminUserDetails;
import com.fitterapp.user.service.query.GetAdminUserService;
import com.fitterapp.user.service.query.ListAdminUsersService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminUserController.class)
@Import(SecurityConfig.class)
class AdminUserAuthorizationTests {
  @Autowired private MockMvc mockMvc;

  @MockitoBean private ListAdminUsersService listAdminUsersService;
  @MockitoBean private GetAdminUserService getAdminUserService;
  @MockitoBean private AdminUserMapper mapper;
  @MockitoBean private JwtDecoder jwtDecoder;

  @Test
  void rejectsAnonymousAccess() throws Exception {
    mockMvc.perform(get("/api/v1/admin/users")).andExpect(status().isUnauthorized());
  }

  @Test
  void rejectsStudentAccess() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/admin/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void rejectsPersonalAccess() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/admin/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PERSONAL"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void allowsAdministratorToListUsers() throws Exception {
    when(listAdminUsersService.list(any(), any(), any(), any()))
        .thenReturn(org.springframework.data.domain.Page.empty());
    when(mapper.toPage(any())).thenReturn(new AdminUserPageDto(List.of(), 0, 20, 0, 0));

    mockMvc
        .perform(
            get("/api/v1/admin/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk());
  }

  @Test
  void allowsOwnerToOpenUserDetail() throws Exception {
    UUID userId = UUID.randomUUID();
    AdminUserDetails details = mock(AdminUserDetails.class);
    AdminUserDetailDto response = mock(AdminUserDetailDto.class);
    when(getAdminUserService.get(userId)).thenReturn(details);
    when(mapper.toDetail(details)).thenReturn(response);

    mockMvc
        .perform(
            get("/api/v1/admin/users/{userId}", userId)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OWNER"))))
        .andExpect(status().isOk());
  }

  @Test
  void returnsNotFoundForUnknownUser() throws Exception {
    UUID userId = UUID.randomUUID();
    when(getAdminUserService.get(userId)).thenThrow(new UserNotFoundException());

    mockMvc
        .perform(
            get("/api/v1/admin/users/{userId}", userId)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
  }
}
