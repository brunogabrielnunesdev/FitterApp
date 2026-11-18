package com.fitterapp.analytics.service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DailyMetricsService {
  private static final int MAX_DAYS = 366;
  private final JdbcTemplate jdbc;

  @Transactional(readOnly = true)
  public List<Day> get(LocalDate from, LocalDate to, String timezone) {
    if (from.isAfter(to)
        || to.equals(LocalDate.MAX)
        || ChronoUnit.DAYS.between(from, to) >= MAX_DAYS)
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Selecione um período de até 366 dias");
    ZoneId zone;
    try {
      zone = ZoneId.of(timezone);
    } catch (DateTimeException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fuso horário inválido");
    }
    OffsetDateTime start = from.atStartOfDay(zone).toOffsetDateTime();
    OffsetDateTime end = to.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
    // Each source is a constant, so identifiers and predicates never come from the request.
    List<Source> sources =
        List.of(
            new Source("accountsRegistered", "users", "created_at", "id", ""),
            new Source("accountsCompleted", "users", "email_verified_at", "id", ""),
            new Source("profilesStarted", "personal_profiles", "created_at", "id", ""),
            new Source(
                "profilesSubmitted",
                "profile_review_events",
                "occurred_at",
                "profile_id",
                " and event_type='SUBMITTED'"),
            new Source(
                "profilesApproved",
                "profile_review_events",
                "occurred_at",
                "profile_id",
                " and event_type='APPROVED'"),
            new Source(
                "profilesRejected",
                "profile_review_events",
                "occurred_at",
                "profile_id",
                " and event_type='REJECTED'"),
            new Source("searches", "search_events", "occurred_at", "id", ""),
            new Source("profileViews", "profile_view_events", "occurred_at", "id", ""),
            new Source("whatsappContacts", "contact_events", "occurred_at", "id", ""),
            new Source(
                "returns", "app_access_events", "occurred_at", "id", " and event_type='RETURNED'"),
            new Source(
                "modality",
                "search_events",
                "occurred_at",
                "id",
                " and jsonb_exists(filters,'modalityId')"),
            new Source(
                "neighborhood",
                "search_events",
                "occurred_at",
                "id",
                " and jsonb_exists(filters,'neighborhood')"),
            new Source(
                "serviceMode",
                "search_events",
                "occurred_at",
                "id",
                " and jsonb_exists(filters,'serviceMode')"));
    Map<LocalDate, Map<String, Long>> days = new LinkedHashMap<>();
    for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1))
      days.put(date, new HashMap<>());
    for (Source source : sources) {
      String sql =
          "select timezone(?, "
              + source.timestamp()
              + ")::date as day, count(distinct "
              + source.identity()
              + ") as total from "
              + source.table()
              + " where "
              + source.timestamp()
              + " >= ? and "
              + source.timestamp()
              + " < ?"
              + source.filter()
              + " group by 1";
      jdbc.query(
          sql,
          rs -> {
            LocalDate day = rs.getObject("day", LocalDate.class);
            if (days.containsKey(day)) days.get(day).put(source.metric(), rs.getLong("total"));
          },
          zone.getId(),
          start,
          end);
    }
    return days.entrySet().stream()
        .map(e -> new Day(e.getKey(), Map.copyOf(e.getValue())))
        .toList();
  }

  private record Source(
      String metric, String table, String timestamp, String identity, String filter) {}

  public record Day(LocalDate date, Map<String, Long> counts) {}
}
