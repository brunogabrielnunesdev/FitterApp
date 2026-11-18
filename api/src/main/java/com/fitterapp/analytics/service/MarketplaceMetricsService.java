package com.fitterapp.analytics.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitterapp.analytics.entity.event.EventSource;
import com.fitterapp.personal.entity.service.ServiceMode;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarketplaceMetricsService {
  private final JdbcTemplate jdbc;
  private final ObjectMapper json;
  private final Clock clock;

  private String clean(String value) {
    return value != null && value.matches("[A-Za-z0-9_-]{1,128}") ? value : null;
  }

  private Context context(UUID userId, EventSource source, String visitorId, String key) {
    String visitor = clean(visitorId), idempotency = clean(key);
    String identity = visitor != null ? "v:" + visitor : userId != null ? "u:" + userId : null;
    return new Context(
        userId, source == null ? EventSource.PUBLIC_WEB : source, visitor, idempotency, identity);
  }

  @Transactional
  public void search(
      UUID user,
      EventSource source,
      String visitor,
      String key,
      String query,
      Short modalityId,
      String neighborhood,
      ServiceMode mode,
      int results) {
    var c = context(user, source, visitor, key);
    Map<String, Object> filters = new LinkedHashMap<>();
    if (modalityId != null) filters.put("modalityId", modalityId);
    if (neighborhood != null && !neighborhood.isBlank())
      filters.put("neighborhood", neighborhood.trim());
    if (mode != null) filters.put("serviceMode", mode.name());
    String term = query == null || query.isBlank() ? null : query.trim();
    if (term != null && term.length() > 120) term = term.substring(0, 120);
    try {
      jdbc.update(
          """
        insert into search_events(id,user_id,source,search_term,filters,result_count,occurred_at,visitor_id,idempotency_key,identity_key)
        values (?, ?, ?, ?, cast(? as jsonb), ?, ?, ?, ?, ?) on conflict do nothing
        """,
          UUID.randomUUID(),
          c.userId(),
          c.source().name(),
          term,
          json.writeValueAsString(filters),
          results,
          OffsetDateTime.now(clock),
          c.visitor(),
          c.key(),
          c.identity());
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Could not encode search filters", e);
    }
  }

  @Transactional
  public void profileView(
      UUID user, EventSource source, String visitor, String key, UUID profileId) {
    var c = context(user, source, visitor, key);
    jdbc.update(
        """
      insert into profile_view_events(id,viewer_user_id,personal_profile_id,source,occurred_at,visitor_id,idempotency_key,identity_key)
      values (?, ?, ?, ?, ?, ?, ?, ?) on conflict do nothing
      """,
        UUID.randomUUID(),
        c.userId(),
        profileId,
        c.source().name(),
        OffsetDateTime.now(clock),
        c.visitor(),
        c.key(),
        c.identity());
  }

  @Transactional
  public void contact(UUID user, EventSource source, String visitor, String key, UUID profileId) {
    var c = context(user, source, visitor, key);
    jdbc.update(
        """
      insert into contact_events(id,user_id,personal_profile_id,source,occurred_at,visitor_id,idempotency_key,identity_key)
      values (?, ?, ?, ?, ?, ?, ?, ?) on conflict do nothing
      """,
        UUID.randomUUID(),
        c.userId(),
        profileId,
        c.source().name(),
        OffsetDateTime.now(clock),
        c.visitor(),
        c.key(),
        c.identity());
  }

  @Transactional
  public void appOpen(UUID user, EventSource source, String visitor, String key) {
    var c = context(user, source, visitor, key);
    jdbc.update(
        """
      insert into app_access_events(id,user_id,event_type,source,occurred_at,visitor_id,idempotency_key,identity_key)
      values (?, ?, case when cast(? as text) is not null and exists (select 1 from app_access_events where identity_key = ?) then 'RETURNED' else 'OPENED' end, ?, ?, ?, ?, ?) on conflict do nothing
      """,
        UUID.randomUUID(),
        c.userId(),
        c.identity(),
        c.identity(),
        c.source().name(),
        OffsetDateTime.now(clock),
        c.visitor(),
        c.key(),
        c.identity());
  }

  private record Context(
      UUID userId, EventSource source, String visitor, String key, String identity) {}
}
