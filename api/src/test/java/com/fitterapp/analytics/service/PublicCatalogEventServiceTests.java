package com.fitterapp.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PublicCatalogEventServiceTests {
  private static final Instant NOW = Instant.parse("2026-08-11T20:00:00Z");
  private final SearchEventRepository searches = mock(SearchEventRepository.class);
  private final ProfileViewEventRepository views = mock(ProfileViewEventRepository.class);
  private final UserRepository users = mock(UserRepository.class);
  private final MetricDeduplicationService deduplication = mock(MetricDeduplicationService.class);
  private PublicCatalogEventService service;

  @BeforeEach
  void setUp() {
    service =
        new PublicCatalogEventService(
            searches, views, users, Clock.fixed(NOW, ZoneOffset.UTC), deduplication);
    when(deduplication.evaluate(any(), any(), any(), any(), any(), any()))
        .thenReturn(new MetricEventDecision(true, true, null));
  }

  @Test
  void recordsSearchTermFiltersResultCountUserSourceAndTimestamp() {
    UUID userId = UUID.randomUUID();
    User user = mock(User.class);
    when(users.findById(userId)).thenReturn(Optional.of(user));

    service.recordSearch(
        userId,
        EventSource.MOBILE_APP,
        " Bruno ",
        (short) 2,
        " Centro ",
        ServiceMode.ONLINE,
        1,
        20,
        42);

    ArgumentCaptor<SearchEvent> captor = ArgumentCaptor.forClass(SearchEvent.class);
    verify(searches).save(captor.capture());
    SearchEvent event = captor.getValue();
    assertThat(event.getUser()).isSameAs(user);
    assertThat(event.getSource()).isEqualTo(EventSource.MOBILE_APP);
    assertThat(event.getSearchTerm()).isEqualTo("Bruno");
    assertThat(event.getFilters().get("modalityId").asInt()).isEqualTo(2);
    assertThat(event.getFilters().get("neighborhood").asText()).isEqualTo("Centro");
    assertThat(event.getFilters().get("serviceMode").asText()).isEqualTo("ONLINE");
    assertThat(event.getFilters().get("page").asInt()).isEqualTo(1);
    assertThat(event.getResultCount()).isEqualTo(42);
    assertThat(event.getOccurredAt().toInstant()).isEqualTo(NOW);
    assertThat(event.isUniqueEvent()).isTrue();
  }

  @Test
  void recordsAnonymousPublicProfileView() {
    Profile profile = mock(Profile.class);

    service.recordPersonalView(null, EventSource.PUBLIC_WEB, profile);

    ArgumentCaptor<ProfileViewEvent> captor = ArgumentCaptor.forClass(ProfileViewEvent.class);
    verify(views).save(captor.capture());
    assertThat(captor.getValue().getViewer()).isNull();
    assertThat(captor.getValue().getPersonalProfile()).isSameAs(profile);
    assertThat(captor.getValue().getSource()).isEqualTo(EventSource.PUBLIC_WEB);
    assertThat(captor.getValue().getOccurredAt().toInstant()).isEqualTo(NOW);
    assertThat(captor.getValue().isUniqueEvent()).isTrue();
    verify(users, never()).findById(any());
  }

  @Test
  void storesSemanticDuplicateAsRawButNotUnique() {
    when(deduplication.evaluate(any(), any(), any(), any(), any(), any()))
        .thenReturn(new MetricEventDecision(true, false, "a".repeat(64)));

    service.recordSearch(
        null,
        EventSource.PUBLIC_WEB,
        "Bruno",
        null,
        null,
        null,
        0,
        20,
        1,
        "visitor-1",
        "request-2");

    ArgumentCaptor<SearchEvent> captor = ArgumentCaptor.forClass(SearchEvent.class);
    verify(searches).save(captor.capture());
    assertThat(captor.getValue().isUniqueEvent()).isFalse();
    assertThat(captor.getValue().getIdempotencyKeyHash()).isEqualTo("a".repeat(64));
  }

  @Test
  void skipsExactIdempotentRetry() {
    when(deduplication.evaluate(any(), any(), any(), any(), any(), any()))
        .thenReturn(new MetricEventDecision(false, false, "b".repeat(64)));

    service.recordPersonalView(
        null,
        EventSource.PUBLIC_WEB,
        mock(Profile.class),
        "visitor-1",
        "same-request");

    verify(views, never()).save(any());
  }
}
