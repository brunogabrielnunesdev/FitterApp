package com.fitterapp.personal.entity.cref;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fitterapp.personal.entity.profile.Profile;
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

@Entity
@Table(name = "personal_crefs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cref {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false)
    private Profile personal;

    @Column(name = "registration_code", nullable = false, unique = true, length = 40)
    private String registrationCode;

    @Column(name = "document_image_key", nullable = false, length = 255)
    private String documentImageKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CrefStatus status;

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


    public static Cref pendingReview(
            Profile personal,
            String registrationCode,
            String documentImageKey,
            OffsetDateTime createdAt) {
        Cref cref = new Cref();
        cref.personal = personal;
        cref.registrationCode = registrationCode;
        cref.documentImageKey = documentImageKey;
        cref.status = CrefStatus.PENDING_REVIEW;
        cref.createdAt = createdAt;
        cref.updatedAt = createdAt;
        return cref;
    }

    public void verify(User verifiedBy, OffsetDateTime verifiedAt) {
        this.status = CrefStatus.VERIFIED;
        this.verifiedBy = verifiedBy;
        this.verifiedAt = verifiedAt;
        this.rejectionReason = null;
        this.updatedAt = verifiedAt;
    }

    public void reject(
            User verifiedBy,
            String rejectionReason,
            OffsetDateTime rejectedAt) {
        this.status = CrefStatus.REJECTED;
        this.verifiedBy = verifiedBy;
        this.verifiedAt = rejectedAt;
        this.rejectionReason = rejectionReason;
        this.updatedAt = rejectedAt;
    }

    public void resubmit(
            String registrationCode,
            String documentImageKey,
            OffsetDateTime updatedAt) {
        this.registrationCode = registrationCode;
        this.documentImageKey = documentImageKey;
        this.status = CrefStatus.PENDING_REVIEW;
        this.verifiedAt = null;
        this.verifiedBy = null;
        this.rejectionReason = null;
        this.updatedAt = updatedAt;
    }










}
