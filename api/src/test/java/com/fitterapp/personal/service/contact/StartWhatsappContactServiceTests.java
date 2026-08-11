package com.fitterapp.personal.service.contact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitterapp.analytics.entity.event.ContactEvent;
import com.fitterapp.analytics.entity.event.EventSource;
import com.fitterapp.analytics.repository.ContactEventRepository;
import com.fitterapp.analytics.service.MetricDeduplicationService;
import com.fitterapp.analytics.service.MetricEventDecision;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StartWhatsappContactServiceTests {

  @Mock private ProfileRepository profiles;

  @Mock private UserRepository users;

  @Mock private ContactEventRepository contactEvents;

  @Mock private MetricDeduplicationService deduplication;

  @Mock private Profile profile;

  @Mock private ProfileRevision revision;

  @Test
  void recordsAnonymousMobileContactAndReturnsNormalizedWhatsappUrl() {
    Clock clock = Clock.fixed(Instant.parse("2026-08-03T22:00:00Z"), ZoneOffset.UTC);
    StartWhatsappContactService service =
        new StartWhatsappContactService(profiles, users, contactEvents, clock, deduplication);
    when(profiles.findPublishedBySlug("bruno-personal")).thenReturn(Optional.of(profile));
    when(profile.getPublishedRevision()).thenReturn(revision);
    when(revision.getWhatsapp()).thenReturn("+55 (44) 99999-9999");
    when(deduplication.evaluate(any(), any(), any(), any(), any(), any()))
        .thenReturn(new MetricEventDecision(true, true, null));

    StartWhatsappContactResult result =
        service.start(new StartWhatsappContactCommand("bruno-personal", null));

    assertThat(result.whatsappUrl()).isEqualTo("https://wa.me/5544999999999");
    ArgumentCaptor<ContactEvent> eventCaptor = ArgumentCaptor.forClass(ContactEvent.class);
    verify(contactEvents).save(eventCaptor.capture());
    assertThat(eventCaptor.getValue().getPersonalProfile()).isSameAs(profile);
    assertThat(eventCaptor.getValue().getUser()).isNull();
    assertThat(eventCaptor.getValue().getSource()).isEqualTo(EventSource.MOBILE_APP);
    verify(users, never()).findById(any());
  }

  @Test
  void recordsTheSourceProvidedByThePublicClient() {
    Clock clock = Clock.fixed(Instant.parse("2026-08-03T22:00:00Z"), ZoneOffset.UTC);
    StartWhatsappContactService service =
        new StartWhatsappContactService(profiles, users, contactEvents, clock, deduplication);
    when(profiles.findPublishedBySlug("bruno-personal")).thenReturn(Optional.of(profile));
    when(profile.getPublishedRevision()).thenReturn(revision);
    when(revision.getWhatsapp()).thenReturn("+5544999999999");
    when(deduplication.evaluate(any(), any(), any(), any(), any(), any()))
        .thenReturn(new MetricEventDecision(true, true, null));

    service.start(
        new StartWhatsappContactCommand("bruno-personal", null, EventSource.PUBLIC_WEB));

    ArgumentCaptor<ContactEvent> eventCaptor = ArgumentCaptor.forClass(ContactEvent.class);
    verify(contactEvents).save(eventCaptor.capture());
    assertThat(eventCaptor.getValue().getSource()).isEqualTo(EventSource.PUBLIC_WEB);
  }

  @Test
  void returnsWhatsappUrlWithoutSavingAnotherRawEventForIdempotentRetry() {
    Clock clock = Clock.fixed(Instant.parse("2026-08-03T22:00:00Z"), ZoneOffset.UTC);
    StartWhatsappContactService service =
        new StartWhatsappContactService(
            profiles, users, contactEvents, clock, deduplication);
    when(profiles.findPublishedBySlug("bruno-personal")).thenReturn(Optional.of(profile));
    when(profile.getPublishedRevision()).thenReturn(revision);
    when(revision.getWhatsapp()).thenReturn("+5544999999999");
    when(deduplication.evaluate(any(), any(), any(), any(), any(), any()))
        .thenReturn(new MetricEventDecision(false, false, "a".repeat(64)));

    var result =
        service.start(
            new StartWhatsappContactCommand(
                "bruno-personal",
                null,
                EventSource.PUBLIC_WEB,
                "visitor-1",
                "same-request"));

    assertThat(result.whatsappUrl()).isEqualTo("https://wa.me/5544999999999");
    verify(contactEvents, never()).save(any());
  }

  @Test
  void storesRepeatedContactAsRawButNotUnique() {
    Clock clock = Clock.fixed(Instant.parse("2026-08-03T22:00:00Z"), ZoneOffset.UTC);
    StartWhatsappContactService service =
        new StartWhatsappContactService(
            profiles, users, contactEvents, clock, deduplication);
    when(profiles.findPublishedBySlug("bruno-personal")).thenReturn(Optional.of(profile));
    when(profile.getPublishedRevision()).thenReturn(revision);
    when(revision.getWhatsapp()).thenReturn("+5544999999999");
    when(deduplication.evaluate(any(), any(), any(), any(), any(), any()))
        .thenReturn(new MetricEventDecision(true, false, "b".repeat(64)));

    service.start(
        new StartWhatsappContactCommand(
            "bruno-personal", null, EventSource.PUBLIC_WEB, "visitor-1", "request-2"));

    ArgumentCaptor<ContactEvent> eventCaptor = ArgumentCaptor.forClass(ContactEvent.class);
    verify(contactEvents).save(eventCaptor.capture());
    assertThat(eventCaptor.getValue().isUniqueEvent()).isFalse();
    assertThat(eventCaptor.getValue().getIdempotencyKeyHash()).isEqualTo("b".repeat(64));
  }
}
