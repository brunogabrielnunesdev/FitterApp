package com.fitterapp.academy.entity.cnpj;

import com.fitterapp.academy.entity.profile.AcademyProfile;
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
@Table(name = "academy_cnpjs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademyCnpj {

  @Id @UuidGenerator private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "academy_id", nullable = false)
  private AcademyProfile academy;

  @Column(
      name = "registration_number",
      nullable = false,
      unique = true,
      columnDefinition = "char(14)")
  @JdbcTypeCode(SqlTypes.CHAR)
  private String registrationNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private AcademyCnpjStatus status;

  @Column(name = "verified_at")
  private OffsetDateTime verifiedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "verified_by")
  private User verifiedBy;

  @Column(name = "rejection_reason", length = 500)
  private String rejectionReason;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  public static AcademyCnpj pendingReview(
      AcademyProfile academy, String registrationNumber, OffsetDateTime createdAt) {
    AcademyCnpj cnpj = new AcademyCnpj();
    cnpj.academy = academy;
    cnpj.registrationNumber = registrationNumber;
    cnpj.status = AcademyCnpjStatus.PENDING_REVIEW;
    cnpj.createdAt = createdAt;
    cnpj.updatedAt = createdAt;
    return cnpj;
  }

  public void verify(User verifiedBy, OffsetDateTime verifiedAt) {
    status = AcademyCnpjStatus.VERIFIED;
    this.verifiedBy = verifiedBy;
    this.verifiedAt = verifiedAt;
    rejectionReason = null;
    updatedAt = verifiedAt;
  }

  public void reject(User verifiedBy, String rejectionReason, OffsetDateTime rejectedAt) {
    status = AcademyCnpjStatus.REJECTED;
    this.verifiedBy = verifiedBy;
    verifiedAt = rejectedAt;
    this.rejectionReason = rejectionReason;
    updatedAt = rejectedAt;
  }
}
