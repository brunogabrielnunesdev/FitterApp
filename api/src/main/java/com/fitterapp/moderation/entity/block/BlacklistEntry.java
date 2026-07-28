package com.fitterapp.moderation.entity.block;

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
@Table(name = "blacklist_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlacklistEntry {

  @Id @UuidGenerator private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "account_block_id", nullable = false)
  private AccountBlock accountBlock;

  @Enumerated(EnumType.STRING)
  @Column(name = "identifier_type", nullable = false, length = 20)
  private BlacklistIdentifierType identifierType;

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "identifier_hash", nullable = false, length = 64)
  private String identifierHash;

  @Column(name = "identifier_suffix", length = 20)
  private String identifierSuffix;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private BlacklistStatus status;

  @Column(name = "expires_at", nullable = false)
  private OffsetDateTime expiresAt;

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

  public static BlacklistEntry active(
      AccountBlock accountBlock,
      BlacklistIdentifierType identifierType,
      String identifierHash,
      String identifierSuffix,
      OffsetDateTime createdAt,
      OffsetDateTime expiresAt) {
    BlacklistEntry entry = new BlacklistEntry();
    entry.accountBlock = accountBlock;
    entry.identifierType = identifierType;
    entry.identifierHash = identifierHash;
    entry.identifierSuffix = identifierSuffix;
    entry.status = BlacklistStatus.ACTIVE;
    entry.createdAt = createdAt;
    entry.updatedAt = createdAt;
    entry.expiresAt = expiresAt;
    return entry;
  }

  public void expire(OffsetDateTime updatedAt) {
    status = BlacklistStatus.EXPIRED;
    this.updatedAt = updatedAt;
  }

  public void revoke(User revokedBy, String revokeReason, OffsetDateTime revokedAt) {
    status = BlacklistStatus.REVOKED;
    this.revokedBy = revokedBy;
    this.revokeReason = revokeReason;
    this.revokedAt = revokedAt;
    updatedAt = revokedAt;
  }
}
