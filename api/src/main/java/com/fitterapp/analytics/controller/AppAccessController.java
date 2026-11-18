package com.fitterapp.analytics.controller;

import com.fitterapp.analytics.entity.event.EventSource;
import com.fitterapp.analytics.service.MarketplaceMetricsService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/events")
@RequiredArgsConstructor
public class AppAccessController {
  private final MarketplaceMetricsService metrics;

  @PostMapping("/app-open")
  public ResponseEntity<Void> open(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam(defaultValue = "MOBILE_APP") EventSource source,
      @RequestHeader(value = "X-Visitor-Id", required = false) String visitor,
      @RequestHeader(value = "X-Idempotency-Key", required = false) String key) {
    metrics.appOpen(jwt == null ? null : UUID.fromString(jwt.getSubject()), source, visitor, key);
    return ResponseEntity.noContent().build();
  }
}
