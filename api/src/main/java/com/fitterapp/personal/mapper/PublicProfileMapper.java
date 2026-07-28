package com.fitterapp.personal.mapper;

import com.fitterapp.personal.dto.publicprofile.*;
import com.fitterapp.personal.entity.profile.Profile;
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
}
