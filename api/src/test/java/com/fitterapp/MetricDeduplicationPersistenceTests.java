package com.fitterapp;

import static org.assertj.core.api.Assertions.assertThat;

import com.fitterapp.analytics.service.MetricDeduplicationService;
import com.fitterapp.analytics.service.MetricEventDecision;
import com.fitterapp.analytics.service.MetricEventType;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class MetricDeduplicationPersistenceTests {
  @Autowired private MetricDeduplicationService service;

  @Test
  void separatesRawUniqueAndIdempotentSearchEventsAcrossTheWindow() {
    UUID userId = UUID.randomUUID();
    OffsetDateTime startedAt = OffsetDateTime.of(2026, 8, 11, 20, 0, 0, 0, ZoneOffset.UTC);
    List<String> fingerprint = List.of("PUBLIC_WEB", "bruno", "2", "centro", "ONLINE");

    MetricEventDecision first =
        service.evaluate(
            MetricEventType.SEARCH,
            userId,
            null,
            "request-1",
            startedAt,
            fingerprint);
    MetricEventDecision semanticDuplicate =
        service.evaluate(
            MetricEventType.SEARCH,
            userId,
            null,
            "request-2",
            startedAt.plusMinutes(1),
            fingerprint);
    MetricEventDecision exactRetry =
        service.evaluate(
            MetricEventType.SEARCH,
            userId,
            null,
            "request-2",
            startedAt.plusMinutes(2),
            fingerprint);
    MetricEventDecision afterWindow =
        service.evaluate(
            MetricEventType.SEARCH,
            userId,
            null,
            "request-3",
            startedAt.plusMinutes(5),
            fingerprint);

    assertThat(first.recordEvent()).isTrue();
    assertThat(first.uniqueEvent()).isTrue();
    assertThat(semanticDuplicate.recordEvent()).isTrue();
    assertThat(semanticDuplicate.uniqueEvent()).isFalse();
    assertThat(exactRetry.recordEvent()).isFalse();
    assertThat(afterWindow.recordEvent()).isTrue();
    assertThat(afterWindow.uniqueEvent()).isTrue();
    assertThat(first.idempotencyKeyHash()).matches("[0-9a-f]{64}");
  }

  @Test
  void usesVisitorIdentityForAnonymousDeduplication() {
    OffsetDateTime occurredAt =
        OffsetDateTime.of(2026, 8, 11, 20, 0, 0, 0, ZoneOffset.UTC);
    List<String> fingerprint = List.of("PUBLIC_WEB", "profile-id");

    var firstVisitor =
        service.evaluate(
            MetricEventType.PROFILE_VIEW,
            null,
            "visitor-1",
            null,
            occurredAt,
            fingerprint);
    var sameVisitor =
        service.evaluate(
            MetricEventType.PROFILE_VIEW,
            null,
            "visitor-1",
            null,
            occurredAt.plusMinutes(29),
            fingerprint);
    var otherVisitor =
        service.evaluate(
            MetricEventType.PROFILE_VIEW,
            null,
            "visitor-2",
            null,
            occurredAt.plusMinutes(1),
            fingerprint);
    var afterWindow =
        service.evaluate(
            MetricEventType.PROFILE_VIEW,
            null,
            "visitor-1",
            null,
            occurredAt.plusMinutes(30),
            fingerprint);

    assertThat(firstVisitor.uniqueEvent()).isTrue();
    assertThat(sameVisitor.uniqueEvent()).isFalse();
    assertThat(otherVisitor.uniqueEvent()).isTrue();
    assertThat(afterWindow.uniqueEvent()).isTrue();
  }

  @Test
  void appliesTenMinuteWindowToWhatsappContact() {
    OffsetDateTime occurredAt =
        OffsetDateTime.of(2026, 8, 11, 20, 0, 0, 0, ZoneOffset.UTC);
    UUID userId = UUID.randomUUID();
    List<String> fingerprint = List.of("MOBILE_APP", "profile-id");

    var first =
        service.evaluate(
            MetricEventType.WHATSAPP_CONTACT,
            userId,
            null,
            null,
            occurredAt,
            fingerprint);
    var beforeWindow =
        service.evaluate(
            MetricEventType.WHATSAPP_CONTACT,
            userId,
            null,
            null,
            occurredAt.plusMinutes(9),
            fingerprint);
    var afterWindow =
        service.evaluate(
            MetricEventType.WHATSAPP_CONTACT,
            userId,
            null,
            null,
            occurredAt.plusMinutes(10),
            fingerprint);

    assertThat(first.uniqueEvent()).isTrue();
    assertThat(beforeWindow.uniqueEvent()).isFalse();
    assertThat(afterWindow.uniqueEvent()).isTrue();
  }

  @Test
  void eliminatesAnonymousExactRetryEvenWithoutVisitorId() {
    OffsetDateTime occurredAt =
        OffsetDateTime.of(2026, 8, 11, 20, 0, 0, 0, ZoneOffset.UTC);

    var first =
        service.evaluate(
            MetricEventType.SEARCH,
            null,
            null,
            "anonymous-request",
            occurredAt,
            List.of("PUBLIC_WEB", "query"));
    var retry =
        service.evaluate(
            MetricEventType.SEARCH,
            null,
            null,
            "anonymous-request",
            occurredAt.plusSeconds(1),
            List.of("PUBLIC_WEB", "query"));

    assertThat(first.recordEvent()).isTrue();
    assertThat(retry.recordEvent()).isFalse();
  }
}
