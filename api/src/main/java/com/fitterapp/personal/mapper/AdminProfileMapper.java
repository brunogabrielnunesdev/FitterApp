package com.fitterapp.personal.mapper;

import com.fitterapp.personal.dto.admin.AdminAccountDto;
import com.fitterapp.personal.dto.admin.AdminCrefDto;
import com.fitterapp.personal.dto.admin.AdminModalityDto;
import com.fitterapp.personal.dto.admin.AdminProfileDetailDto;
import com.fitterapp.personal.dto.admin.AdminProfilePageDto;
import com.fitterapp.personal.dto.admin.AdminProfileRevisionDto;
import com.fitterapp.personal.dto.admin.AdminProfileSummaryDto;
import com.fitterapp.personal.dto.admin.AdminServiceAreaDto;
import com.fitterapp.personal.entity.cref.Cref;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.service.query.AdminProfileDetails;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class AdminProfileMapper {

  public AdminProfilePageDto toPage(Page<Profile> page) {
    return new AdminProfilePageDto(
        page.getContent().stream().map(this::toSummary).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }

  public AdminProfileDetailDto toDetail(AdminProfileDetails details) {
    var profile = details.profile();
    var revision = details.revision();
    var user = profile.getUser();

    return new AdminProfileDetailDto(
        profile.getId(),
        profile.getSlug(),
        profile.getStatus(),
        profile.isPublished(),
        profile.getPublishedRevision() == null ? null : profile.getPublishedRevision().getId(),
        profile.getPublishedAt(),
        profile.getCreatedAt(),
        profile.getUpdatedAt(),
        new AdminAccountDto(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getStatus()),
        toRevision(details));
  }

  private AdminProfileSummaryDto toSummary(Profile profile) {
    var revision = profile.getCurrentRevision();
    return new AdminProfileSummaryDto(
        profile.getId(),
        revision == null ? null : revision.getId(),
        revision == null ? profile.getFullName() : revision.getFullName(),
        profile.getUser().getEmail(),
        profile.getStatus(),
        revision == null ? null : revision.getStatus(),
        profile.isPublished(),
        revision == null ? null : revision.getSubmittedAt(),
        profile.getUpdatedAt());
  }

  private AdminProfileRevisionDto toRevision(AdminProfileDetails details) {
    ProfileRevision revision = details.revision();
    return new AdminProfileRevisionDto(
        revision.getId(),
        revision.getVersionNumber(),
        revision.getStatus(),
        revision.isRequiresReview(),
        revision.getRejectionReason(),
        revision.getFullName(),
        revision.getBiography(),
        revision.getWhatsapp(),
        revision.getProfileImageKey(),
        revision.getExperienceStartedYear(),
        revision.getCertifications(),
        revision.getGymsDescription(),
        revision.getStartingPriceCents(),
        revision.getPriceUnit(),
        toCref(revision.getCref()),
        details.modalities().stream()
            .map(
                link ->
                    new AdminModalityDto(
                        link.getModality().getId(),
                        link.getModality().getName(),
                        link.getModality().getSlug(),
                        link.getModality().isActive()))
            .toList(),
        details.serviceModes().stream().map(mode -> mode.getServiceMode()).toList(),
        details.serviceAreas().stream()
            .map(
                area ->
                    new AdminServiceAreaDto(
                        area.getId(),
                        area.getCity(),
                        area.getStateCode(),
                        area.getNeighborhood(),
                        area.getDescription()))
            .toList(),
        revision.getSubmittedAt(),
        revision.getReviewedAt(),
        revision.getReviewedBy() == null ? null : revision.getReviewedBy().getId(),
        revision.getCreatedAt(),
        revision.getUpdatedAt());
  }

  private AdminCrefDto toCref(Cref cref) {
    if (cref == null) {
      return null;
    }
    return new AdminCrefDto(
        cref.getId(),
        cref.getRegistrationCode(),
        cref.getDocumentImageKey(),
        cref.getStatus(),
        cref.getRejectionReason(),
        cref.getVerifiedAt());
  }
}
