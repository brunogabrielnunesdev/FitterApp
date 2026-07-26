package com.fitterapp.moderation.entity.report;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "moderation_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_profile_id")
    private Profile personalProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_profile_id")
    private AcademyProfile academyProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ReportReason reason;

    @Column(length = 1500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ReportResolution resolution;

    @Column(name = "resolution_note", length = 1500)
    private String resolutionNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public static Report againstPersonal(
            User reporter,
            Profile personal,
            ReportReason reason,
            String description,
            OffsetDateTime createdAt) {
        Report report = open(reporter, reason, description, createdAt);
        report.personalProfile = personal;
        return report;
    }

    public static Report againstAcademy(
            User reporter,
            AcademyProfile academy,
            ReportReason reason,
            String description,
            OffsetDateTime createdAt) {
        Report report = open(reporter, reason, description, createdAt);
        report.academyProfile = academy;
        return report;
    }

    private static Report open(
            User reporter,
            ReportReason reason,
            String description,
            OffsetDateTime createdAt) {
        Report report = new Report();
        report.reporter = reporter;
        report.reason = reason;
        report.description = description;
        report.status = ReportStatus.OPEN;
        report.priority = ReportPriority.NORMAL;
        report.createdAt = createdAt;
        report.updatedAt = createdAt;
        return report;
    }

    public void startReview(ReportPriority priority, OffsetDateTime updatedAt) {
        status = ReportStatus.UNDER_REVIEW;
        this.priority = priority;
        this.updatedAt = updatedAt;
    }

    public void resolve(
            ReportResolution resolution,
            String resolutionNote,
            User reviewedBy,
            OffsetDateTime reviewedAt) {
        status = ReportStatus.RESOLVED;
        this.resolution = resolution;
        this.resolutionNote = resolutionNote;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        updatedAt = reviewedAt;
    }

    public void dismiss(
            String resolutionNote,
            User reviewedBy,
            OffsetDateTime reviewedAt) {
        status = ReportStatus.DISMISSED;
        resolution = ReportResolution.NO_ACTION;
        this.resolutionNote = resolutionNote;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        updatedAt = reviewedAt;
    }
}
