package com.fitterapp.analytics.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fitterapp.analytics.entity.event.EventSource;
import com.fitterapp.analytics.entity.event.SearchEvent;
import com.fitterapp.user.entity.User;
import java.time.OffsetDateTime;

public final class SearchEventActions {
  private SearchEventActions() {}

  public static SearchEvent record(
      User user,
      EventSource source,
      String searchTerm,
      JsonNode filters,
      int resultCount,
      OffsetDateTime occurredAt) {
    SearchEvent event = new SearchEvent();
    event.setUser(user);
    event.setSource(source);
    event.setSearchTerm(searchTerm);
    event.setFilters(filters);
    event.setResultCount(resultCount);
    event.setOccurredAt(occurredAt);
    return event;
  }
}
