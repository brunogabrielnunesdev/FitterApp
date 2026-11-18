package com.fitterapp.personal.service.query;

import com.fitterapp.personal.entity.cref.*;
import com.fitterapp.personal.entity.profile.*;
import com.fitterapp.personal.entity.service.*;
import com.fitterapp.personal.repository.*;
import com.fitterapp.user.entity.UserStatus;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProfileQueryService {
  private final ProfileRepository profiles;
  private final RevisionModalityRepository modalities;
  private final RevisionServiceModeRepository modes;
  private final RevisionServiceAreaRepository areas;

  public ProfilePage list(ProfileStatus status, int page, int size) {
    var result =
        profiles.findAll(
            (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status),
            PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by("id"))));
    return new ProfilePage(
        result.getContent().stream().map(this::summary).toList(),
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages());
  }

  private Summary summary(Profile p) {
    var r = p.getCurrentRevision();
    var user = p.getUser();
    return new Summary(
        p.getId(),
        r == null ? null : r.getId(),
        r == null || r.getFullName() == null ? p.getFullName() : r.getFullName(),
        user == null ? null : user.getEmail(),
        p.getStatus(),
        r == null ? null : r.getStatus(),
        p.isPublished(),
        r == null ? null : r.getSubmittedAt(),
        p.getUpdatedAt());
  }

  public Detail get(UUID id) {
    var p =
        profiles
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil não encontrado"));
    var u = p.getUser();
    var r = p.getCurrentRevision();
    if (u == null || r == null)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Perfil sem conta ou revisão atual");
    var c = r.getCref();
    var cref =
        c == null
            ? null
            : new CrefView(
                c.getId(),
                c.getRegistrationCode(),
                c.getDocumentImageKey(),
                c.getStatus(),
                c.getRejectionReason(),
                c.getVerifiedAt());
    var mod =
        modalities.findAllByRevisionIdOrderByModalityNameAsc(r.getId()).stream()
            .map(x -> x.getModality())
            .map(x -> new ModalityView(x.getId(), x.getName(), x.getSlug(), x.isActive()))
            .toList();
    var serviceModes =
        modes.findAllByRevisionIdOrderByIdServiceModeAsc(r.getId()).stream()
            .map(x -> x.getServiceMode())
            .toList();
    var serviceAreas =
        areas.findAllByRevisionIdOrderByCityAscNeighborhoodAsc(r.getId()).stream()
            .map(
                x ->
                    new AreaView(
                        x.getId(),
                        x.getCity(),
                        x.getStateCode(),
                        x.getNeighborhood(),
                        x.getDescription()))
            .toList();
    var account =
        new AccountView(
            u.getId(), u.getFullName(), u.getEmail(), u.getPhoneNumber(), u.getStatus());
    var rev =
        new RevisionView(
            r.getId(),
            r.getVersionNumber(),
            r.getStatus(),
            r.isRequiresReview(),
            r.getRejectionReason(),
            r.getFullName(),
            r.getBiography(),
            r.getWhatsapp(),
            r.getProfileImageKey(),
            r.getExperienceStartedYear(),
            r.getCertifications(),
            r.getGymsDescription(),
            r.getStartingPriceCents(),
            r.getPriceUnit(),
            cref,
            mod,
            serviceModes,
            serviceAreas,
            r.getSubmittedAt(),
            r.getReviewedAt(),
            r.getReviewedBy() == null ? null : r.getReviewedBy().getId(),
            r.getCreatedAt(),
            r.getUpdatedAt());
    return new Detail(
        p.getId(),
        p.getSlug(),
        p.getStatus(),
        p.isPublished(),
        p.getPublishedRevision() == null ? null : p.getPublishedRevision().getId(),
        p.getPublishedAt(),
        p.getCreatedAt(),
        p.getUpdatedAt(),
        account,
        rev);
  }

  public record Summary(
      UUID profileId,
      UUID revisionId,
      String fullName,
      String email,
      ProfileStatus profileStatus,
      ProfileRevisionStatus revisionStatus,
      boolean published,
      OffsetDateTime submittedAt,
      OffsetDateTime updatedAt) {}

  public record ProfilePage(
      List<Summary> content, int page, int size, long totalElements, int totalPages) {}

  public record AccountView(
      UUID userId, String fullName, String email, String phoneNumber, UserStatus status) {}

  public record CrefView(
      UUID id,
      String registrationCode,
      String documentImageKey,
      CrefStatus status,
      String rejectionReason,
      OffsetDateTime verifiedAt) {}

  public record ModalityView(Short id, String name, String slug, boolean active) {}

  public record AreaView(
      UUID id, String city, String stateCode, String neighborhood, String description) {}

  public record RevisionView(
      UUID revisionId,
      Integer versionNumber,
      ProfileRevisionStatus status,
      boolean requiresReview,
      String rejectionReason,
      String fullName,
      String biography,
      String whatsapp,
      String profileImageKey,
      Short experienceStartedYear,
      String certifications,
      String gymsDescription,
      Integer startingPriceCents,
      PriceUnit priceUnit,
      CrefView cref,
      List<ModalityView> modalities,
      List<ServiceMode> serviceModes,
      List<AreaView> serviceAreas,
      OffsetDateTime submittedAt,
      OffsetDateTime reviewedAt,
      UUID reviewedByUserId,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {}

  public record Detail(
      UUID profileId,
      String slug,
      ProfileStatus status,
      boolean published,
      UUID publishedRevisionId,
      OffsetDateTime publishedAt,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt,
      AccountView account,
      RevisionView revision) {}
}
