package com.fitterapp.analytics.controller;

import com.fitterapp.analytics.service.DailyMetricsService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class DailyMetricsController {
  private final DailyMetricsService service;

  @GetMapping("/daily")
  public List<DailyMetricsService.Day> daily(
      @RequestParam LocalDate from,
      @RequestParam LocalDate to,
      @RequestParam(defaultValue = "America/Sao_Paulo") String timezone) {
    return service.get(from, to, timezone);
  }
}
