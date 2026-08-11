package com.fitterapp.analytics.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitterapp.analytics.dto.dashboard.AdminDashboardDto;
import com.fitterapp.analytics.dto.dashboard.DashboardPeriodDto;
import com.fitterapp.analytics.dto.dashboard.EventMetricsDto;
import com.fitterapp.analytics.dto.dashboard.FunnelMetricsDto;
import com.fitterapp.analytics.exception.InvalidDashboardPeriodException;
import com.fitterapp.analytics.service.DashboardQueryService;
import com.fitterapp.auth.security.SecurityConfig;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminDashboardController.class)
@Import(SecurityConfig.class)
class AdminDashboardAuthorizationTests {
  private static final String URL = "/api/v1/admin/dashboard/funnel";
  private static final LocalDate FROM = LocalDate.parse("2026-08-01");
  private static final LocalDate TO = LocalDate.parse("2026-08-31");

  @Autowired private MockMvc mockMvc;
  @MockitoBean private DashboardQueryService dashboardQueryService;
  @MockitoBean private JwtDecoder jwtDecoder;

  @Test
  void rejectsAnonymousAccess() throws Exception {
    mockMvc.perform(get(URL).param("from", FROM.toString()).param("to", TO.toString()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void rejectsStudentAccess() throws Exception {
    mockMvc
        .perform(
            get(URL)
                .param("from", FROM.toString())
                .param("to", TO.toString())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void rejectsPersonalAccess() throws Exception {
    mockMvc
        .perform(
            get(URL)
                .param("from", FROM.toString())
                .param("to", TO.toString())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PERSONAL"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void returnsDashboardToAdministrator() throws Exception {
    when(dashboardQueryService.query(FROM, TO, "America/Sao_Paulo"))
        .thenReturn(response());

    mockMvc
        .perform(
            get(URL)
                .param("from", FROM.toString())
                .param("to", TO.toString())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.period.timezone").value("America/Sao_Paulo"))
        .andExpect(jsonPath("$.funnel.accountsCompleted").value(10))
        .andExpect(jsonPath("$.searches.raw").value(30))
        .andExpect(jsonPath("$.searches.unique").value(20));
  }

  @Test
  void allowsOwnerAndForwardsRequestedTimezone() throws Exception {
    when(dashboardQueryService.query(FROM, TO, "UTC")).thenReturn(response());

    mockMvc
        .perform(
            get(URL)
                .param("from", FROM.toString())
                .param("to", TO.toString())
                .param("timezone", "UTC")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OWNER"))))
        .andExpect(status().isOk());
  }

  @Test
  void returnsBadRequestForAnInvalidPeriod() throws Exception {
    when(dashboardQueryService.query(FROM, TO, "Invalid/Zone"))
        .thenThrow(new InvalidDashboardPeriodException("Dashboard timezone is invalid"));

    mockMvc
        .perform(
            get(URL)
                .param("from", FROM.toString())
                .param("to", TO.toString())
                .param("timezone", "Invalid/Zone")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_DASHBOARD_PERIOD"));
  }

  private AdminDashboardDto response() {
    return new AdminDashboardDto(
        new DashboardPeriodDto(
            FROM,
            TO,
            "America/Sao_Paulo",
            OffsetDateTime.parse("2026-08-01T00:00:00-03:00"),
            OffsetDateTime.parse("2026-09-01T00:00:00-03:00")),
        new FunnelMetricsDto(10, 8, 7, 5, 2),
        new EventMetricsDto(30, 20),
        new EventMetricsDto(25, 18),
        new EventMetricsDto(9, 6));
  }
}
