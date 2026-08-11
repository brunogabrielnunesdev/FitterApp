package com.fitterapp.analytics.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MetricDeduplicationService {
  private final JdbcTemplate jdbcTemplate;

  @Transactional
  public MetricEventDecision evaluate(
      MetricEventType eventType,
      UUID userId,
      String visitorId,
      String idempotencyKey,
      OffsetDateTime occurredAt,
      List<String> fingerprintParts) {
    String normalizedIdempotencyKey = normalize(idempotencyKey);
    String actor = actor(userId, visitorId, normalizedIdempotencyKey);
    String actorHash = hash(actor);
    String idempotencyKeyHash =
        normalizedIdempotencyKey == null ? null : hash(normalizedIdempotencyKey);
    if (idempotencyKeyHash != null && !claimIdempotency(
        eventType, actorHash, idempotencyKeyHash, occurredAt)) {
      return MetricEventDecision.skipped(idempotencyKeyHash);
    }

    String fingerprint = fingerprint(eventType, actor, fingerprintParts);
    boolean unique = claimUnique(eventType, hash(fingerprint), occurredAt);
    return MetricEventDecision.record(unique, idempotencyKeyHash);
  }

  private boolean claimIdempotency(
      MetricEventType eventType,
      String actorHash,
      String idempotencyKeyHash,
      OffsetDateTime occurredAt) {
    return jdbcTemplate.update(
            """
            INSERT INTO metric_idempotency_keys (
                event_type, actor_hash, idempotency_key_hash, claimed_at
            ) VALUES (?, ?, ?, ?)
            ON CONFLICT DO NOTHING
            """,
            eventType.name(),
            actorHash,
            idempotencyKeyHash,
            occurredAt)
        == 1;
  }

  private boolean claimUnique(
      MetricEventType eventType, String fingerprintHash, OffsetDateTime occurredAt) {
    OffsetDateTime cutoff = occurredAt.minus(eventType.deduplicationWindow());
    return jdbcTemplate.update(
            """
            INSERT INTO metric_unique_states (
                event_type, fingerprint_hash, last_unique_at
            ) VALUES (?, ?, ?)
            ON CONFLICT (event_type, fingerprint_hash) DO UPDATE
            SET last_unique_at = EXCLUDED.last_unique_at
            WHERE metric_unique_states.last_unique_at <= ?
            """,
            eventType.name(),
            fingerprintHash,
            occurredAt,
            cutoff)
        == 1;
  }

  private String actor(UUID userId, String visitorId, String idempotencyKey) {
    if (userId != null) return "user:" + userId;
    String normalizedVisitor = normalize(visitorId);
    if (normalizedVisitor != null) return "visitor:" + normalizedVisitor;
    if (idempotencyKey != null) return "idempotency:" + idempotencyKey;
    return "request:" + UUID.randomUUID();
  }

  private String fingerprint(
      MetricEventType eventType, String actor, List<String> fingerprintParts) {
    StringBuilder value = new StringBuilder(eventType.name()).append('|').append(actor);
    for (String part : fingerprintParts) {
      String safePart = part == null ? "<null>" : part;
      value.append('|').append(safePart.length()).append(':').append(safePart);
    }
    return value.toString();
  }

  private String normalize(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  private String hash(String value) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }
}
