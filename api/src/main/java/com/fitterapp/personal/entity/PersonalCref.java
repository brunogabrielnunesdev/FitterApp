package com.fitterapp.personal.entity;

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

@Entity
@Table(name = "personal_crefs")
public class PersonalCref {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false)
    private PersonalProfile personal;

    @Column(name = "registration_code", nullable = false, unique = true, length = 40)
    private String registrationCode;

    @Column(name = "document_image_key", nullable = false, length = 255)
    private String documentImageKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PersonalCrefStatus status;

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

    protected PersonalCref() {
    }

    public static PersonalCref pendingReview(
            PersonalProfile personal,
            String registrationCode,
            String documentImageKey,
            OffsetDateTime createdAt) {
        PersonalCref cref = new PersonalCref();
        cref.personal = personal;
        cref.registrationCode = registrationCode;
        cref.documentImageKey = documentImageKey;
        cref.status = PersonalCrefStatus.PENDING_REVIEW;
        cref.createdAt = createdAt;
        cref.updatedAt = createdAt;
        return cref;
    }

    public void verify(User verifiedBy, OffsetDateTime verifiedAt) {
        this.status = PersonalCrefStatus.VERIFIED;
        this.verifiedBy = verifiedBy;
        this.verifiedAt = verifiedAt;
        this.rejectionReason = null;
        this.updatedAt = verifiedAt;
    }

    public void reject(
            User verifiedBy,
            String rejectionReason,
            OffsetDateTime rejectedAt) {
        this.status = PersonalCrefStatus.REJECTED;
        this.verifiedBy = verifiedBy;
        this.verifiedAt = rejectedAt;
        this.rejectionReason = rejectionReason;
        this.updatedAt = rejectedAt;
    }

    public UUID getId() {
        return id;
    }

    public PersonalProfile getPersonal() {
        return personal;
    }

    public String getRegistrationCode() {
        return registrationCode;
    }

    public String getDocumentImageKey() {
        return documentImageKey;
    }

    public PersonalCrefStatus getStatus() {
        return status;
    }

    public OffsetDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public User getVerifiedBy() {
        return verifiedBy;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
