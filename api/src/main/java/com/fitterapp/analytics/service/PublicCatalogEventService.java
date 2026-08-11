package com.fitterapp.analytics.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fitterapp.analytics.entity.event.EventSource;
import com.fitterapp.analytics.entity.event.ProfileViewEvent;
import com.fitterapp.analytics.entity.event.SearchEvent;
import com.fitterapp.analytics.repository.ProfileViewEventRepository;
import com.fitterapp.analytics.repository.SearchEventRepository;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.service.ServiceMode;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.repository.UserRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublicCatalogEventService {
  private final SearchEventRepository searchEvents;
  private final ProfileViewEventRepository profileViewEvents;
  private final UserRepository users;
  private final Clock clock;
  private final MetricDeduplicationService deduplication;

  @Transactional
  public void recordSearch(
      UUID userId,
      EventSource source,
      String query,
      Short modalityId,
      String neighborhood,
      ServiceMode serviceMode,
      int page,
      int size,
      long resultCount) {
    recordSearch(
        userId,
        source,
        query,
        modalityId,
        neighborhood,
        serviceMode,
        page,
        size,
        resultCount,
        null,
        null);
  }

  @Transactional
  public void recordSearch(
      UUID userId,
      EventSource source,
      String query,
      Short modalityId,
      String neighborhood,
      ServiceMode serviceMode,
      int page,
      int size,
      long resultCount,
      String visitorId,
      String idempotencyKey) {
    String normalizedQuery = normalize(query);
    ObjectNode filters = JsonNodeFactory.instance.objectNode();
    if (modalityId != null) filters.put("modalityId", modalityId);
    String normalizedNeighborhood = normalize(neighborhood);
    if (normalizedNeighborhood != null) filters.put("neighborhood", normalizedNeighborhood);
    if (serviceMode != null) filters.put("serviceMode", serviceMode.name());
    filters.put("page", page);
    filters.put("size", size);
    OffsetDateTime occurredAt = OffsetDateTime.now(clock);
    MetricEventDecision decision =
        deduplication.evaluate(
            MetricEventType.SEARCH,
            userId,
            visitorId,
            idempotencyKey,
            occurredAt,
            List.of(
                source.name(),
                value(normalizedQuery),
                value(modalityId),
                value(normalizedNeighborhood),
                value(serviceMode)));
    if (!decision.recordEvent()) return;
    searchEvents.save(
        SearchEvent.record(
            user(userId),
            source,
            normalizedQuery,
            filters,
            (int) Math.min(resultCount, Integer.MAX_VALUE),
            occurredAt,
            decision.uniqueEvent(),
            decision.idempotencyKeyHash()));
  }

  @Transactional
  public void recordPersonalView(UUID userId, EventSource source, Profile profile) {
    recordPersonalView(userId, source, profile, null, null);
  }

  @Transactional
  public void recordPersonalView(
      UUID userId,
      EventSource source,
      Profile profile,
      String visitorId,
      String idempotencyKey) {
    OffsetDateTime occurredAt = OffsetDateTime.now(clock);
    MetricEventDecision decision =
        deduplication.evaluate(
            MetricEventType.PROFILE_VIEW,
            userId,
            visitorId,
            idempotencyKey,
            occurredAt,
            List.of(source.name(), value(profile.getId())));
    if (!decision.recordEvent()) return;
    profileViewEvents.save(
        ProfileViewEvent.personalView(
            user(userId),
            profile,
            source,
            null,
            occurredAt,
            decision.uniqueEvent(),
            decision.idempotencyKeyHash()));
  }

  private User user(UUID userId) {
    return userId == null ? null : users.findById(userId).orElse(null);
  }

  private String normalize(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  private String value(Object value) {
    return value == null ? "" : value.toString();
  }
}
