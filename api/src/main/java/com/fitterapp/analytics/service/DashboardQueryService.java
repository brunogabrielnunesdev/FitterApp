package com.fitterapp.analytics.service;

import com.fitterapp.analytics.dto.dashboard.AdminDashboardDto;
import com.fitterapp.analytics.dto.dashboard.DashboardPeriodDto;
import com.fitterapp.analytics.dto.dashboard.EventMetricsDto;
import com.fitterapp.analytics.dto.dashboard.FunnelMetricsDto;
import com.fitterapp.analytics.exception.InvalidDashboardPeriodException;
import com.fitterapp.analytics.repository.DashboardMetricSnapshot;
import com.fitterapp.analytics.repository.DashboardMetricsRepository;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardQueryService {
  private final DashboardMetricsRepository repository;

  @Transactional(readOnly = true)
  public AdminDashboardDto query(LocalDate from, LocalDate to, String timezone) {
    if (from == null || to == null) {
      throw new InvalidDashboardPeriodException("Dashboard period is required");
    }
    if (from.isAfter(to)) {
      throw new InvalidDashboardPeriodException("Dashboard period start must not be after end");
    }

    ZoneId zone = parseZone(timezone);
    OffsetDateTime startInclusive = from.atStartOfDay(zone).toOffsetDateTime();
    OffsetDateTime endExclusive = to.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
    DashboardMetricSnapshot snapshot = repository.aggregate(startInclusive, endExclusive);

    return new AdminDashboardDto(
        new DashboardPeriodDto(from, to, zone.getId(), startInclusive, endExclusive),
        new FunnelMetricsDto(
            snapshot.accountsCompleted(),
            snapshot.profilesStarted(),
            snapshot.profilesSubmitted(),
            snapshot.profilesApproved(),
            snapshot.profilesRejected()),
        new EventMetricsDto(snapshot.searchesRaw(), snapshot.searchesUnique()),
        new EventMetricsDto(snapshot.profileViewsRaw(), snapshot.profileViewsUnique()),
        new EventMetricsDto(
            snapshot.whatsappContactsRaw(), snapshot.whatsappContactsUnique()));
  }

  private ZoneId parseZone(String timezone) {
    if (timezone == null || timezone.isBlank()) {
      throw new InvalidDashboardPeriodException("Dashboard timezone is required");
    }
    try {
      return ZoneId.of(timezone.trim());
    } catch (DateTimeException exception) {
      throw new InvalidDashboardPeriodException("Dashboard timezone is invalid");
    }
  }
}
