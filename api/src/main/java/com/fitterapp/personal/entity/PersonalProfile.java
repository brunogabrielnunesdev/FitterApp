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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "personal_profiles")
public class PersonalProfile {

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
    private PersonalProfileStatus status;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_revision_id")
    private PersonalProfileRevision currentRevision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_revision_id")
    private PersonalProfileRevision publishedRevision;

    protected PersonalProfile() {
    }

    public static PersonalProfile draft(
            String fullName,
            String slug,
            OffsetDateTime createdAt) {
        PersonalProfile profile = new PersonalProfile();
        profile.fullName = fullName;
        profile.slug = slug;
        profile.status = PersonalProfileStatus.DRAFT;
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
        status = PersonalProfileStatus.PUBLISHED;
        this.publishedAt = publishedAt;
        updatedAt = publishedAt;
    }

    public void setCurrentRevision(
            PersonalProfileRevision revision,
            OffsetDateTime updatedAt) {
        currentRevision = revision;
        this.updatedAt = updatedAt;
    }

    public void submitForReview(OffsetDateTime submittedAt) {
        status = PersonalProfileStatus.PENDING_REVIEW;
        updatedAt = submittedAt;
    }

    public void approve(OffsetDateTime approvedAt) {
        status = PersonalProfileStatus.APPROVED;
        updatedAt = approvedAt;
    }

    public void reject(OffsetDateTime rejectedAt) {
        status = PersonalProfileStatus.REJECTED;
        updatedAt = rejectedAt;
    }

    public void publish(
            PersonalProfileRevision revision,
            OffsetDateTime publishedAt) {
        currentRevision = revision;
        publishedRevision = revision;
        status = PersonalProfileStatus.PUBLISHED;
        this.publishedAt = publishedAt;
        updatedAt = publishedAt;
    }

    public void unpublish(OffsetDateTime unpublishedAt) {
        status = PersonalProfileStatus.APPROVED;
        publishedRevision = null;
        publishedAt = null;
        updatedAt = unpublishedAt;
    }

    public void suspend(OffsetDateTime suspendedAt) {
        status = PersonalProfileStatus.SUSPENDED;
        updatedAt = suspendedAt;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSlug() {
        return slug;
    }

    public String getBiography() {
        return biography;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public String getProfileImageKey() {
        return profileImageKey;
    }

    public Short getExperienceStartedYear() {
        return experienceStartedYear;
    }

    public String getCertifications() {
        return certifications;
    }

    public String getGymsDescription() {
        return gymsDescription;
    }

    public Integer getStartingPriceCents() {
        return startingPriceCents;
    }

    public PriceUnit getPriceUnit() {
        return priceUnit;
    }

    public PersonalProfileStatus getStatus() {
        return status;
    }

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public PersonalProfileRevision getCurrentRevision() {
        return currentRevision;
    }

    public PersonalProfileRevision getPublishedRevision() {
        return publishedRevision;
    }
}
