package com.fitterapp.analytics.controller;

import com.fitterapp.analytics.service.AdminDashboardService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {
  private final AdminDashboardService service;

  @GetMapping("/funnel")
  public AdminDashboardService.Dashboard funnel(
      @RequestParam LocalDate from,
      @RequestParam LocalDate to,
      @RequestParam(defaultValue = "America/Sao_Paulo") String timezone) {
    return service.get(from, to, timezone);
  }
}
