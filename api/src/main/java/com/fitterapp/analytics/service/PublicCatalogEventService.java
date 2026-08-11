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
    String normalizedQuery = normalize(query);
    ObjectNode filters = JsonNodeFactory.instance.objectNode();
    if (modalityId != null) filters.put("modalityId", modalityId);
    String normalizedNeighborhood = normalize(neighborhood);
    if (normalizedNeighborhood != null) filters.put("neighborhood", normalizedNeighborhood);
    if (serviceMode != null) filters.put("serviceMode", serviceMode.name());
    filters.put("page", page);
    filters.put("size", size);
    searchEvents.save(
        SearchEvent.record(
            user(userId),
            source,
            normalizedQuery,
            filters,
            (int) Math.min(resultCount, Integer.MAX_VALUE),
            OffsetDateTime.now(clock)));
  }

  @Transactional
  public void recordPersonalView(UUID userId, EventSource source, Profile profile) {
    profileViewEvents.save(
        ProfileViewEvent.personalView(
            user(userId), profile, source, null, OffsetDateTime.now(clock)));
  }

  private User user(UUID userId) {
    return userId == null ? null : users.findById(userId).orElse(null);
  }

  private String normalize(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
