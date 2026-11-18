package com.fitterapp.analytics.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fitterapp.analytics.entity.audit.AdminAuditLog;
import com.fitterapp.analytics.entity.audit.AuditTargetType;
import com.fitterapp.moderation.entity.report.Report;
import com.fitterapp.user.entity.User;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class AdminAuditLogActions {
  private AdminAuditLogActions() {}

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
    log.setActor(actor);
    log.setAction(action);
    log.setTargetType(targetType);
    log.setTargetId(targetId);
    log.setReason(reason);
    log.setReport(report);
    log.setPreviousState(previousState);
    log.setNewState(newState);
    log.setCreatedAt(createdAt);
    return log;
  }
}
