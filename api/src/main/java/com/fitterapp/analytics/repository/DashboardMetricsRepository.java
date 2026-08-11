package com.fitterapp.analytics.repository;

import java.time.OffsetDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DashboardMetricsRepository {
  private static final String AGGREGATE_QUERY =
      """
      WITH funnel AS (
          SELECT
              count(*) FILTER (WHERE event_type = 'ACCOUNT_COMPLETED') AS accounts_completed,
              count(*) FILTER (WHERE event_type = 'PROFILE_STARTED') AS profiles_started,
              count(*) FILTER (WHERE event_type = 'PROFILE_SUBMITTED') AS profiles_submitted
          FROM funnel_events
          WHERE occurred_at >= :start AND occurred_at < :end
      ), reviews AS (
          SELECT
              count(*) FILTER (WHERE status = 'APPROVED') AS profiles_approved,
              count(*) FILTER (WHERE status = 'REJECTED') AS profiles_rejected
          FROM personal_profile_revisions
          WHERE reviewed_at >= :start AND reviewed_at < :end
      ), searches AS (
          SELECT
              count(*) AS raw_count,
              count(*) FILTER (WHERE unique_event) AS unique_count
          FROM search_events
          WHERE occurred_at >= :start AND occurred_at < :end
      ), profile_views AS (
          SELECT
              count(*) AS raw_count,
              count(*) FILTER (WHERE unique_event) AS unique_count
          FROM profile_view_events
          WHERE occurred_at >= :start AND occurred_at < :end
      ), whatsapp_contacts AS (
          SELECT
              count(*) AS raw_count,
              count(*) FILTER (WHERE unique_event) AS unique_count
          FROM contact_events
          WHERE occurred_at >= :start AND occurred_at < :end
      )
      SELECT
          funnel.accounts_completed,
          funnel.profiles_started,
          funnel.profiles_submitted,
          reviews.profiles_approved,
          reviews.profiles_rejected,
          searches.raw_count AS searches_raw,
          searches.unique_count AS searches_unique,
          profile_views.raw_count AS profile_views_raw,
          profile_views.unique_count AS profile_views_unique,
          whatsapp_contacts.raw_count AS whatsapp_contacts_raw,
          whatsapp_contacts.unique_count AS whatsapp_contacts_unique
      FROM funnel
      CROSS JOIN reviews
      CROSS JOIN searches
      CROSS JOIN profile_views
      CROSS JOIN whatsapp_contacts
      """;

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public DashboardMetricSnapshot aggregate(
      OffsetDateTime startInclusive, OffsetDateTime endExclusive) {
    return jdbcTemplate.queryForObject(
        AGGREGATE_QUERY,
        Map.of("start", startInclusive, "end", endExclusive),
        (resultSet, rowNumber) ->
            new DashboardMetricSnapshot(
                resultSet.getLong("accounts_completed"),
                resultSet.getLong("profiles_started"),
                resultSet.getLong("profiles_submitted"),
                resultSet.getLong("profiles_approved"),
                resultSet.getLong("profiles_rejected"),
                resultSet.getLong("searches_raw"),
                resultSet.getLong("searches_unique"),
                resultSet.getLong("profile_views_raw"),
                resultSet.getLong("profile_views_unique"),
                resultSet.getLong("whatsapp_contacts_raw"),
                resultSet.getLong("whatsapp_contacts_unique")));
  }
}
