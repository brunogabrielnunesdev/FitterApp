package com.fitterapp.analytics.entity.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fitterapp.moderation.entity.report.Report;
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
@Table(name = "admin_audit_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminAuditLog {

  @Id @UuidGenerator private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "actor_user_id", nullable = false)
  private User actor;

  @Column(nullable = false, length = 80)
  private String action;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, length = 30)
  private AuditTargetType targetType;

  @Column(name = "target_id", nullable = false)
  private UUID targetId;

  @Column(length = 1500)
  private String reason;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "report_id")
  private Report report;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "previous_state", columnDefinition = "jsonb")
  private JsonNode previousState;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "new_state", columnDefinition = "jsonb")
  private JsonNode newState;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  public static AdminAuditLog record(
      User actor,
      String action,
      AuditTargetType targetType,
      UUID targetId,
      String reason,
      Report report,
      JsonNode previousState,
      JsonNode newState,
      OffsetDateTime createdAt) {
    AdminAuditLog log = new AdminAuditLog();
    log.actor = actor;
    log.action = action;
    log.targetType = targetType;
    log.targetId = targetId;
    log.reason = reason;
    log.report = report;
    log.previousState = previousState;
    log.newState = newState;
    log.createdAt = createdAt;
    return log;
  }
}
