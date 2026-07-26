package com.fitterapp.moderation.entity.appeal;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.fitterapp.moderation.entity.block.AccountBlock;
import com.fitterapp.moderation.entity.suspension.ProfileSuspension;
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
@Table(name = "moderation_appeals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ModerationAppeal {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suspension_id")
    private ProfileSuspension suspension;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_block_id")
    private AccountBlock accountBlock;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appellant_user_id", nullable = false)
    private User appellant;

    @Column(nullable = false, length = 1500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppealStatus status;

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

    public static ModerationAppeal forSuspension(
            ProfileSuspension suspension,
            User appellant,
            String reason,
            OffsetDateTime createdAt) {
        ModerationAppeal appeal = pending(appellant, reason, createdAt);
        appeal.suspension = suspension;
        return appeal;
    }

    public static ModerationAppeal forAccountBlock(
            AccountBlock accountBlock,
            User appellant,
            String reason,
            OffsetDateTime createdAt) {
        ModerationAppeal appeal = pending(appellant, reason, createdAt);
        appeal.accountBlock = accountBlock;
        return appeal;
    }

    private static ModerationAppeal pending(
            User appellant,
            String reason,
            OffsetDateTime createdAt) {
        ModerationAppeal appeal = new ModerationAppeal();
        appeal.appellant = appellant;
        appeal.reason = reason;
        appeal.status = AppealStatus.PENDING;
        appeal.createdAt = createdAt;
        appeal.updatedAt = createdAt;
        return appeal;
    }

    public void approve(
            User reviewedBy,
            String reviewNote,
            OffsetDateTime reviewedAt) {
        review(AppealStatus.APPROVED, reviewedBy, reviewNote, reviewedAt);
    }

    public void reject(
            User reviewedBy,
            String reviewNote,
            OffsetDateTime reviewedAt) {
        review(AppealStatus.REJECTED, reviewedBy, reviewNote, reviewedAt);
    }

    private void review(
            AppealStatus status,
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
