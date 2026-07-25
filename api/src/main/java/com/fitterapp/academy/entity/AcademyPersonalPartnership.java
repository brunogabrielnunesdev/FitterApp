package com.fitterapp.academy.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.fitterapp.personal.entity.PersonalProfile;
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
@Table(name = "academy_personal_partnerships")
public class AcademyPersonalPartnership {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academy_id", nullable = false)
    private AcademyProfile academy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false)
    private PersonalProfile personal;

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

    protected AcademyPersonalPartnership() {
    }

    public static AcademyPersonalPartnership request(
            AcademyProfile academy,
            PersonalProfile personal,
            PartnershipInitiator initiatedBy,
            User requestedBy,
            OffsetDateTime requestedAt) {
        AcademyPersonalPartnership partnership =
                new AcademyPersonalPartnership();
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

    public UUID getId() {
        return id;
    }

    public AcademyProfile getAcademy() {
        return academy;
    }

    public PersonalProfile getPersonal() {
        return personal;
    }

    public PartnershipInitiator getInitiatedBy() {
        return initiatedBy;
    }

    public User getRequestedBy() {
        return requestedBy;
    }

    public PartnershipStatus getStatus() {
        return status;
    }

    public User getRespondedBy() {
        return respondedBy;
    }

    public OffsetDateTime getRequestedAt() {
        return requestedAt;
    }

    public OffsetDateTime getRespondedAt() {
        return respondedAt;
    }

    public OffsetDateTime getEndedAt() {
        return endedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
