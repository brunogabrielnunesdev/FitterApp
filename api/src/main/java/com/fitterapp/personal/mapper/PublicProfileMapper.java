package com.fitterapp.personal.mapper;

import com.fitterapp.personal.dto.publicprofile.*;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.service.publicprofile.PublicProfileDetails;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PublicProfileMapper {
  public PublicProfileCardDto toCard(Profile p) {
    var r = p.getPublishedRevision();
    return new PublicProfileCardDto(
        p.getId(),
        p.getSlug(),
        r.getFullName(),
        r.getBiography(),
        r.getProfileImageKey(),
        r.getStartingPriceCents(),
        r.getPriceUnit());
  }

  public PublicProfilePageDto toPage(Page<Profile> page) {
    return new PublicProfilePageDto(
        page.map(this::toCard).getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }

  public PublicProfileDetailDto toDetail(PublicProfileDetails details) {
    var profile = details.profile();
    var revision = details.revision();
    return new PublicProfileDetailDto(
        profile.getId(),
        profile.getSlug(),
        revision.getFullName(),
        revision.getBiography(),
        revision.getProfileImageKey(),
        revision.getExperienceStartedYear(),
        revision.getCertifications(),
        revision.getGymsDescription(),
        revision.getStartingPriceCents(),
        revision.getPriceUnit(),
        details.modalities().stream()
            .map(link -> link.getModality())
            .map(
                modality ->
                    new PublicModalityDto(modality.getId(), modality.getName(), modality.getSlug()))
            .toList(),
        details.serviceModes().stream().map(mode -> mode.getServiceMode()).toList(),
        details.serviceAreas().stream()
            .map(
                area ->
                    new PublicServiceAreaDto(
                        area.getCity(),
                        area.getStateCode(),
                        area.getNeighborhood(),
                        area.getDescription()))
            .toList());
  }
}
