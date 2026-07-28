package com.fitterapp.moderation.entity.block;

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
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "account_blocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountBlock {

  @Id @UuidGenerator private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "report_id")
  private Report report;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "blocked_by", nullable = false)
  private User blockedBy;

  @Column(nullable = false, length = 1500)
  private String reason;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AccountBlockStatus status;

  @Column(name = "blocked_at", nullable = false)
  private OffsetDateTime blockedAt;

  @Column(name = "revoked_at")
  private OffsetDateTime revokedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "revoked_by")
  private User revokedBy;

  @Column(name = "revoke_reason", length = 1500)
  private String revokeReason;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  public static AccountBlock block(
      User user, Report report, User blockedBy, String reason, OffsetDateTime blockedAt) {
    AccountBlock block = new AccountBlock();
    block.user = user;
    block.report = report;
    block.blockedBy = blockedBy;
    block.reason = reason;
    block.status = AccountBlockStatus.ACTIVE;
    block.blockedAt = blockedAt;
    block.createdAt = blockedAt;
    block.updatedAt = blockedAt;
    return block;
  }

  public void revoke(User revokedBy, String revokeReason, OffsetDateTime revokedAt) {
    status = AccountBlockStatus.REVOKED;
    this.revokedBy = revokedBy;
    this.revokeReason = revokeReason;
    this.revokedAt = revokedAt;
    updatedAt = revokedAt;
  }
}
