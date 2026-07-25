package com.fitterapp.personal.entity.profile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fitterapp.personal.entity.service.PriceUnit;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "personal_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {

    @Id
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String slug;

    @Column(length = 1500)
    private String biography;

    @Column(length = 20)
    private String whatsapp;

    @Column(name = "profile_image_key", length = 255)
    private String profileImageKey;

    @Column(name = "experience_started_year")
    private Short experienceStartedYear;

    @Column(length = 1000)
    private String certifications;

    @Column(name = "gyms_description", length = 500)
    private String gymsDescription;

    @Column(name = "starting_price_cents")
    private Integer startingPriceCents;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_unit", length = 30)
    private PriceUnit priceUnit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProfileStatus status;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_revision_id")
    private ProfileRevision currentRevision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_revision_id")
    private ProfileRevision publishedRevision;


    public static Profile draft(
            String fullName,
            String slug,
            OffsetDateTime createdAt) {
        Profile profile = new Profile();
        profile.fullName = fullName;
        profile.slug = slug;
        profile.status = ProfileStatus.DRAFT;
        profile.createdAt = createdAt;
        profile.updatedAt = createdAt;
        return profile;
    }

    public void linkUser(User user, OffsetDateTime updatedAt) {
        this.user = user;
        this.updatedAt = updatedAt;
    }

    public void updateProfessionalDetails(
            String biography,
            String whatsapp,
            Short experienceStartedYear,
            String certifications,
            String gymsDescription,
            OffsetDateTime updatedAt) {
        this.biography = biography;
        this.whatsapp = whatsapp;
        this.experienceStartedYear = experienceStartedYear;
        this.certifications = certifications;
        this.gymsDescription = gymsDescription;
        this.updatedAt = updatedAt;
    }

    public void updateProfileImage(String profileImageKey, OffsetDateTime updatedAt) {
        this.profileImageKey = profileImageKey;
        this.updatedAt = updatedAt;
    }

    public void updateStartingPrice(
            Integer startingPriceCents,
            PriceUnit priceUnit,
            OffsetDateTime updatedAt) {
        this.startingPriceCents = startingPriceCents;
        this.priceUnit = priceUnit;
        this.updatedAt = updatedAt;
    }

    public void publish(OffsetDateTime publishedAt) {
        status = ProfileStatus.PUBLISHED;
        this.publishedAt = publishedAt;
        updatedAt = publishedAt;
    }

    public void setCurrentRevision(
            ProfileRevision revision,
            OffsetDateTime updatedAt) {
        currentRevision = revision;
        this.updatedAt = updatedAt;
    }

    public void submitForReview(OffsetDateTime submittedAt) {
        status = ProfileStatus.PENDING_REVIEW;
        updatedAt = submittedAt;
    }

    public void approve(OffsetDateTime approvedAt) {
        status = ProfileStatus.APPROVED;
        updatedAt = approvedAt;
    }

    public void reject(OffsetDateTime rejectedAt) {
        status = ProfileStatus.REJECTED;
        updatedAt = rejectedAt;
    }

    public void publish(
            ProfileRevision revision,
            OffsetDateTime publishedAt) {
        currentRevision = revision;
        publishedRevision = revision;
        status = ProfileStatus.PUBLISHED;
        this.publishedAt = publishedAt;
        updatedAt = publishedAt;
    }

    public void unpublish(OffsetDateTime unpublishedAt) {
        status = ProfileStatus.APPROVED;
        publishedRevision = null;
        publishedAt = null;
        updatedAt = unpublishedAt;
    }

    public void suspend(OffsetDateTime suspendedAt) {
        status = ProfileStatus.SUSPENDED;
        updatedAt = suspendedAt;
    }


















}
