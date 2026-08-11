package com.fitterapp.analytics.controller;

import com.fitterapp.analytics.dto.dashboard.AdminDashboardDto;
import com.fitterapp.analytics.service.DashboardQueryService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {
  private final DashboardQueryService dashboardQueryService;

  @GetMapping("/funnel")
  public ResponseEntity<AdminDashboardDto> funnel(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(defaultValue = "America/Sao_Paulo") String timezone) {
    return ResponseEntity.ok(dashboardQueryService.query(from, to, timezone));
  }
}
