package com.fitterapp.analytics.entity.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fitterapp.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "search_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchEvent {

  @Id @UuidGenerator private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EventSource source;

  @Column(name = "search_term", length = 120)
  private String searchTerm;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private JsonNode filters;

  @Column(name = "result_count", nullable = false)
  private int resultCount;

  @Column(name = "unique_event", nullable = false)
  private boolean uniqueEvent;

  @Column(name = "idempotency_key_hash", length = 64)
  private String idempotencyKeyHash;

  @Column(name = "occurred_at", nullable = false, updatable = false)
  private OffsetDateTime occurredAt;

  public static SearchEvent record(
      User user,
      EventSource source,
      String searchTerm,
      JsonNode filters,
      int resultCount,
      OffsetDateTime occurredAt) {
    return record(user, source, searchTerm, filters, resultCount, occurredAt, true, null);
  }

  public static SearchEvent record(
      User user,
      EventSource source,
      String searchTerm,
      JsonNode filters,
      int resultCount,
      OffsetDateTime occurredAt,
      boolean uniqueEvent,
      String idempotencyKeyHash) {
    SearchEvent event = new SearchEvent();
    event.user = user;
    event.source = source;
    event.searchTerm = searchTerm;
    event.filters = filters;
    event.resultCount = resultCount;
    event.occurredAt = occurredAt;
    event.uniqueEvent = uniqueEvent;
    event.idempotencyKeyHash = idempotencyKeyHash;
    return event;
  }
}
