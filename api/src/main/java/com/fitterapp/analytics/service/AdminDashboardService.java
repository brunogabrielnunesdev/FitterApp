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
    Long registered =
        count("select count(*) from users where created_at >= ? and created_at < ?", start, end);
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
            "select count(distinct profile_id) from profile_review_events where event_type='SUBMITTED' and occurred_at >= ? and occurred_at < ?",
            start,
            end);
    Long approved =
        count(
            "select count(distinct profile_id) from profile_review_events where event_type='APPROVED' and occurred_at >= ? and occurred_at < ?",
            start,
            end);
    Long rejected =
        count(
            "select count(distinct profile_id) from profile_review_events where event_type='REJECTED' and occurred_at >= ? and occurred_at < ?",
            start,
            end);
    Event searches = event("search_events", "user_id", start, end);
    Event views = event("profile_view_events", "viewer_user_id", start, end);
    Event contacts = event("contact_events", "user_id", start, end);
    Event returns = event("app_access_events", "user_id", start, end, " and event_type='RETURNED'");
    Long modality =
        count(
            "select count(*) from search_events where jsonb_exists(filters, 'modalityId') and occurred_at >= ? and occurred_at < ?",
            start,
            end);
    Long neighborhood =
        count(
            "select count(*) from search_events where jsonb_exists(filters, 'neighborhood') and occurred_at >= ? and occurred_at < ?",
            start,
            end);
    Long serviceMode =
        count(
            "select count(*) from search_events where jsonb_exists(filters, 'serviceMode') and occurred_at >= ? and occurred_at < ?",
            start,
            end);
    OffsetDateTime trackingStartedAt =
        jdbc.queryForObject("select started_at from metrics_tracking_start", OffsetDateTime.class);
    return new Dashboard(
        new Period(from, to, zone.getId(), start, end),
        new Funnel(registered, accounts, profiles, submitted, approved, rejected),
        searches,
        views,
        contacts,
        returns,
        new Filters(modality, neighborhood, serviceMode),
        trackingStartedAt);
  }

  private Event event(String table, String userColumn, OffsetDateTime start, OffsetDateTime end) {
    return event(table, userColumn, start, end, "");
  }

  private Event event(
      String table, String userColumn, OffsetDateTime start, OffsetDateTime end, String filter) {
    String sql =
        "select count(*) as total, count(distinct coalesce(identity_key, 'u:' || "
            + userColumn
            + "::text)) as unique_count from "
            + table
            + " where occurred_at >= ? and occurred_at < ?"
            + filter;
    return jdbc.queryForObject(
        sql, (rs, row) -> new Event(rs.getLong("total"), rs.getLong("unique_count")), start, end);
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
      Long accountsRegistered,
      Long accountsCompleted,
      Long profilesStarted,
      Long profilesSubmitted,
      Long profilesApproved,
      Long profilesRejected) {}

  public record Event(Long raw, Long unique) {}

  public record Filters(Long modality, Long neighborhood, Long serviceMode) {}

  public record Dashboard(
      Period period,
      Funnel funnel,
      Event searches,
      Event profileViews,
      Event whatsappContacts,
      Event returns,
      Filters filters,
      OffsetDateTime trackingStartedAt) {}
}
