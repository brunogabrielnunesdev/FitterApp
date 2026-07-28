package com.fitterapp.academy.entity.partnership;

import com.fitterapp.academy.entity.profile.AcademyProfile;
import com.fitterapp.personal.entity.profile.Profile;
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
@Table(name = "academy_personal_partnerships")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademyPersonalPartnership {

  @Id @UuidGenerator private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "academy_id", nullable = false)
  private AcademyProfile academy;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "personal_id", nullable = false)
  private Profile personal;

  @Enumerated(EnumType.STRING)
  @Column(name = "initiated_by", nullable = false, length = 20)
  private PartnershipInitiator initiatedBy;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "requested_by", nullable = false)
  private User requestedBy;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PartnershipStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "responded_by")
  private User respondedBy;

  @Column(name = "requested_at", nullable = false)
  private OffsetDateTime requestedAt;

  @Column(name = "responded_at")
  private OffsetDateTime respondedAt;

  @Column(name = "ended_at")
  private OffsetDateTime endedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  public static AcademyPersonalPartnership request(
      AcademyProfile academy,
      Profile personal,
      PartnershipInitiator initiatedBy,
      User requestedBy,
      OffsetDateTime requestedAt) {
    AcademyPersonalPartnership partnership = new AcademyPersonalPartnership();
    partnership.academy = academy;
    partnership.personal = personal;
    partnership.initiatedBy = initiatedBy;
    partnership.requestedBy = requestedBy;
    partnership.status = PartnershipStatus.PENDING;
    partnership.requestedAt = requestedAt;
    partnership.createdAt = requestedAt;
    partnership.updatedAt = requestedAt;
    return partnership;
  }

  public void accept(User respondedBy, OffsetDateTime respondedAt) {
    status = PartnershipStatus.ACCEPTED;
    this.respondedBy = respondedBy;
    this.respondedAt = respondedAt;
    updatedAt = respondedAt;
  }

  public void reject(User respondedBy, OffsetDateTime respondedAt) {
    status = PartnershipStatus.REJECTED;
    this.respondedBy = respondedBy;
    this.respondedAt = respondedAt;
    updatedAt = respondedAt;
  }

  public void end(OffsetDateTime endedAt) {
    status = PartnershipStatus.ENDED;
    this.endedAt = endedAt;
    updatedAt = endedAt;
  }
}
