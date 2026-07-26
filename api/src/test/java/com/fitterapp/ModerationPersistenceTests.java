package com.fitterapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.fitterapp.moderation.entity.appeal.AppealStatus;
import com.fitterapp.moderation.entity.appeal.ModerationAppeal;
import com.fitterapp.moderation.entity.block.AccountBlock;
import com.fitterapp.moderation.entity.block.BlacklistEntry;
import com.fitterapp.moderation.entity.block.BlacklistIdentifierType;
import com.fitterapp.moderation.entity.block.BlacklistStatus;
import com.fitterapp.moderation.entity.report.Report;
import com.fitterapp.moderation.entity.report.ReportEvidence;
import com.fitterapp.moderation.entity.report.ReportPriority;
import com.fitterapp.moderation.entity.report.ReportReason;
import com.fitterapp.moderation.entity.report.ReportResolution;
import com.fitterapp.moderation.entity.report.ReportStatus;
import com.fitterapp.moderation.entity.suspension.ProfileSuspension;
import com.fitterapp.moderation.entity.suspension.ReactivationRequest;
import com.fitterapp.moderation.entity.suspension.ReactivationRequestStatus;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.user.entity.User;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ModerationPersistenceTests {

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsReportEvidenceAndResolution() {
        OffsetDateTime createdAt = now();
        User reporter = activeUser(
                "Reporting Student",
                "moderation-reporter@fitterapp.com",
                "+5544999999930",
                createdAt);
        User reviewer = activeUser(
                "Moderation Admin",
                "moderation-reviewer@fitterapp.com",
                "+5544999999931",
                createdAt);
        Profile personal = personal(
                reporter,
                "reported-personal",
                createdAt);

        Report report = Report.againstPersonal(
                reporter,
                personal,
                ReportReason.FALSE_INFORMATION,
                "Informacao profissional divergente",
                createdAt);
        entityManager.persist(report);
        entityManager.flush();

        ReportEvidence evidence = ReportEvidence.attach(
                report,
                (short) 1,
                "moderation/reports/" + report.getId() + "/"
                        + UUID.randomUUID() + ".webp",
                createdAt);
        entityManager.persist(evidence);

        OffsetDateTime reviewedAt = createdAt.plusMinutes(10);
        report.startReview(ReportPriority.HIGH, createdAt.plusMinutes(5));
        report.resolve(
                ReportResolution.CORRECTION_REQUESTED,
                "Solicitada correcao dos dados publicos",
                reviewer,
                reviewedAt);

        entityManager.flush();
        entityManager.clear();

        Report savedReport = entityManager.find(Report.class, report.getId());
        ReportEvidence savedEvidence = entityManager.find(
                ReportEvidence.class,
                evidence.getId());

        assertThat(savedReport.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(savedReport.getPriority()).isEqualTo(ReportPriority.HIGH);
        assertThat(savedReport.getResolution())
                .isEqualTo(ReportResolution.CORRECTION_REQUESTED);
        assertThat(savedReport.getPersonalProfile().getId())
                .isEqualTo(personal.getId());
        assertThat(savedReport.getReviewedBy().getId())
                .isEqualTo(reviewer.getId());
        assertThat(savedEvidence.getReport().getId()).isEqualTo(report.getId());
        assertThat(savedEvidence.getPosition()).isEqualTo((short) 1);
    }

    @Test
    void persistsSuspensionAndApprovedReactivationRequest() {
        OffsetDateTime createdAt = now();
        User personalUser = activeUser(
                "Suspended Personal",
                "suspended-personal@fitterapp.com",
                "+5544999999932",
                createdAt);
        User admin = activeUser(
                "Suspension Admin",
                "suspension-admin@fitterapp.com",
                "+5544999999933",
                createdAt);
        Profile personal = personal(
                personalUser,
                "suspended-personal",
                createdAt);

        ProfileSuspension suspension = ProfileSuspension.suspendPersonal(
                personal,
                null,
                admin,
                "Perfil precisa de correcao",
                "PUBLISHED",
                createdAt,
                createdAt.plusDays(7));
        entityManager.persist(suspension);

        ReactivationRequest request = ReactivationRequest.request(
                suspension,
                personalUser,
                "As informacoes foram corrigidas",
                createdAt.plusDays(7));
        entityManager.persist(request);

        OffsetDateTime reviewedAt = createdAt.plusDays(7).plusHours(1);
        request.approve(admin, "Correcao validada", reviewedAt);
        suspension.lift(admin, "Perfil regularizado", reviewedAt);

        entityManager.flush();
        entityManager.clear();

        ReactivationRequest savedRequest = entityManager.find(
                ReactivationRequest.class,
                request.getId());
        ProfileSuspension savedSuspension = entityManager.find(
                ProfileSuspension.class,
                suspension.getId());

        assertThat(savedRequest.getStatus())
                .isEqualTo(ReactivationRequestStatus.APPROVED);
        assertThat(savedRequest.getSuspension().getId())
                .isEqualTo(suspension.getId());
        assertThat(savedSuspension.getStatus().name()).isEqualTo("LIFTED");
        assertThat(savedSuspension.getLiftedBy().getId()).isEqualTo(admin.getId());
        assertThat(savedSuspension.getPreviousStatus()).isEqualTo("PUBLISHED");
    }

    @Test
    void persistsAccountBlockBlacklistAndAppeal() {
        OffsetDateTime createdAt = now();
        User blockedUser = activeUser(
                "Blocked User",
                "blocked-user@fitterapp.com",
                "+5544999999934",
                createdAt);
        User admin = activeUser(
                "Block Admin",
                "block-admin@fitterapp.com",
                "+5544999999935",
                createdAt);

        AccountBlock block = AccountBlock.block(
                blockedUser,
                null,
                admin,
                "Fraude confirmada",
                createdAt);
        entityManager.persist(block);

        BlacklistEntry entry = BlacklistEntry.active(
                block,
                BlacklistIdentifierType.EMAIL,
                "b".repeat(64),
                "com",
                createdAt,
                createdAt.plusYears(2));
        entityManager.persist(entry);

        ModerationAppeal appeal = ModerationAppeal.forAccountBlock(
                block,
                blockedUser,
                "Solicito uma nova analise",
                createdAt.plusMinutes(5));
        entityManager.persist(appeal);
        appeal.reject(
                admin,
                "Evidencias do bloqueio confirmadas",
                createdAt.plusMinutes(20));

        entityManager.flush();
        entityManager.clear();

        AccountBlock savedBlock = entityManager.find(
                AccountBlock.class,
                block.getId());
        BlacklistEntry savedEntry = entityManager.find(
                BlacklistEntry.class,
                entry.getId());
        ModerationAppeal savedAppeal = entityManager.find(
                ModerationAppeal.class,
                appeal.getId());

        assertThat(savedBlock.getUser().getId()).isEqualTo(blockedUser.getId());
        assertThat(savedEntry.getStatus()).isEqualTo(BlacklistStatus.ACTIVE);
        assertThat(savedEntry.getIdentifierType())
                .isEqualTo(BlacklistIdentifierType.EMAIL);
        assertThat(savedEntry.getAccountBlock().getId()).isEqualTo(block.getId());
        assertThat(savedAppeal.getStatus()).isEqualTo(AppealStatus.REJECTED);
        assertThat(savedAppeal.getAccountBlock().getId()).isEqualTo(block.getId());
        assertThat(savedAppeal.getReviewedBy().getId()).isEqualTo(admin.getId());
    }

    private User activeUser(
            String fullName,
            String email,
            String phone,
            OffsetDateTime createdAt) {
        User user = User.pendingRegistration(
                fullName,
                email,
                phone,
                "test-password-hash",
                createdAt);
        user.confirmEmail(createdAt);
        entityManager.persist(user);
        return user;
    }

    private Profile personal(
            User user,
            String slug,
            OffsetDateTime createdAt) {
        Profile profile = Profile.draft(user.getFullName(), slug, createdAt);
        profile.linkUser(user, createdAt);
        entityManager.persist(profile);
        return profile;
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
    }
}
