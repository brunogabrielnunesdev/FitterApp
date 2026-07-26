package com.fitterapp.moderation.entity.suspension;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

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
@Table(name = "reactivation_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReactivationRequest {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "suspension_id", nullable = false)
    private ProfileSuspension suspension;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @Column(nullable = false, length = 1500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReactivationRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "review_note", length = 1500)
    private String reviewNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public static ReactivationRequest request(
            ProfileSuspension suspension,
            User requestedBy,
            String reason,
            OffsetDateTime createdAt) {
        ReactivationRequest request = new ReactivationRequest();
        request.suspension = suspension;
        request.requestedBy = requestedBy;
        request.reason = reason;
        request.status = ReactivationRequestStatus.PENDING;
        request.createdAt = createdAt;
        request.updatedAt = createdAt;
        return request;
    }

    public void approve(
            User reviewedBy,
            String reviewNote,
            OffsetDateTime reviewedAt) {
        review(ReactivationRequestStatus.APPROVED, reviewedBy, reviewNote, reviewedAt);
    }

    public void reject(
            User reviewedBy,
            String reviewNote,
            OffsetDateTime reviewedAt) {
        review(ReactivationRequestStatus.REJECTED, reviewedBy, reviewNote, reviewedAt);
    }

    private void review(
            ReactivationRequestStatus status,
            User reviewedBy,
            String reviewNote,
            OffsetDateTime reviewedAt) {
        this.status = status;
        this.reviewedBy = reviewedBy;
        this.reviewNote = reviewNote;
        this.reviewedAt = reviewedAt;
        updatedAt = reviewedAt;
    }
}
