package com.fitterapp.analytics.service;

import java.time.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {
  private final JdbcTemplate jdbc;

  @Transactional(readOnly = true)
  public Dashboard get(LocalDate from, LocalDate to, String timezone) {
    if (from.isAfter(to) || to.equals(LocalDate.MAX))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Período inválido");
    ZoneId zone;
    try {
      zone = ZoneId.of(timezone);
    } catch (DateTimeException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fuso horário inválido");
    }
    OffsetDateTime start = from.atStartOfDay(zone).toOffsetDateTime();
    OffsetDateTime end = to.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
    Long accounts =
        count(
            "select count(*) from users where email_verified_at >= ? and email_verified_at < ?",
            start,
            end);
    Long profiles =
        count(
            "select count(*) from personal_profiles where created_at >= ? and created_at < ?",
            start,
            end);
    Long submitted =
        count(
            "select count(distinct personal_id) from personal_profile_revisions where submitted_at >= ? and submitted_at < ?",
            start,
            end);
    Long contacts =
        count(
            "select count(*) from contact_events where occurred_at >= ? and occurred_at < ?",
            start,
            end);
    return new Dashboard(
        new Period(from, to, zone.getId(), start, end),
        new Funnel(accounts, profiles, submitted, null, null),
        new Event(null, null),
        new Event(null, null),
        new Event(contacts, null));
  }

  private Long count(String sql, OffsetDateTime start, OffsetDateTime end) {
    return jdbc.queryForObject(sql, Long.class, start, end);
  }

  public record Period(
      LocalDate from,
      LocalDate to,
      String timezone,
      OffsetDateTime startInclusive,
      OffsetDateTime endExclusive) {}

  public record Funnel(
      Long accountsCompleted,
      Long profilesStarted,
      Long profilesSubmitted,
      Long profilesApproved,
      Long profilesRejected) {}

  public record Event(Long raw, Long unique) {}

  public record Dashboard(
      Period period, Funnel funnel, Event searches, Event profileViews, Event whatsappContacts) {}
}
