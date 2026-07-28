package com.fitterapp.moderation.entity.report;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "moderation_report_evidences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportEvidence {

  @Id @UuidGenerator private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "report_id", nullable = false)
  private Report report;

  @Column(nullable = false)
  private Short position;

  @Column(name = "storage_key", nullable = false, length = 255)
  private String storageKey;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  public static ReportEvidence attach(
      Report report, short position, String storageKey, OffsetDateTime createdAt) {
    ReportEvidence evidence = new ReportEvidence();
    evidence.report = report;
    evidence.position = position;
    evidence.storageKey = storageKey;
    evidence.createdAt = createdAt;
    return evidence;
  }
}
