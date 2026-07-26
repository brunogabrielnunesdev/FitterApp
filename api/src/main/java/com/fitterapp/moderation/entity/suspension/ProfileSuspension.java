package com.fitterapp.moderation.entity.suspension;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.fitterapp.academy.entity.profile.AcademyProfile;
import com.fitterapp.moderation.entity.report.Report;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profile_suspensions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileSuspension {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_profile_id")
    private Profile personalProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_profile_id")
    private AcademyProfile academyProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "suspended_by", nullable = false)
    private User suspendedBy;

    @Column(nullable = false, length = 1500)
    private String reason;

    @Column(name = "previous_status", nullable = false, length = 20)
    private String previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SuspensionStatus status;

    @Column(name = "suspended_at", nullable = false)
    private OffsetDateTime suspendedAt;

    @Column(name = "eligible_for_reactivation_at", nullable = false)
    private OffsetDateTime eligibleForReactivationAt;

    @Column(name = "lifted_at")
    private OffsetDateTime liftedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lifted_by")
    private User liftedBy;

    @Column(name = "lift_reason", length = 1500)
    private String liftReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public static ProfileSuspension suspendPersonal(
            Profile personal,
            Report report,
            User suspendedBy,
            String reason,
            String previousStatus,
            OffsetDateTime suspendedAt,
            OffsetDateTime eligibleForReactivationAt) {
        ProfileSuspension suspension = active(
                report,
                suspendedBy,
                reason,
                previousStatus,
                suspendedAt,
                eligibleForReactivationAt);
        suspension.personalProfile = personal;
        return suspension;
    }

    public static ProfileSuspension suspendAcademy(
            AcademyProfile academy,
            Report report,
            User suspendedBy,
            String reason,
            String previousStatus,
            OffsetDateTime suspendedAt,
            OffsetDateTime eligibleForReactivationAt) {
        ProfileSuspension suspension = active(
                report,
                suspendedBy,
                reason,
                previousStatus,
                suspendedAt,
                eligibleForReactivationAt);
        suspension.academyProfile = academy;
        return suspension;
    }

    private static ProfileSuspension active(
            Report report,
            User suspendedBy,
            String reason,
            String previousStatus,
            OffsetDateTime suspendedAt,
            OffsetDateTime eligibleForReactivationAt) {
        ProfileSuspension suspension = new ProfileSuspension();
        suspension.report = report;
        suspension.suspendedBy = suspendedBy;
        suspension.reason = reason;
        suspension.previousStatus = previousStatus;
        suspension.status = SuspensionStatus.ACTIVE;
        suspension.suspendedAt = suspendedAt;
        suspension.eligibleForReactivationAt = eligibleForReactivationAt;
        suspension.createdAt = suspendedAt;
        suspension.updatedAt = suspendedAt;
        return suspension;
    }

    public void lift(
            User liftedBy,
            String liftReason,
            OffsetDateTime liftedAt) {
        status = SuspensionStatus.LIFTED;
        this.liftedBy = liftedBy;
        this.liftReason = liftReason;
        this.liftedAt = liftedAt;
        updatedAt = liftedAt;
    }
}
