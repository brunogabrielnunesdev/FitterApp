package com.fitterapp.personal.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fitterapp.personal.entity.cref.Cref;
import com.fitterapp.personal.entity.cref.CrefStatus;
import com.fitterapp.personal.entity.modality.Modality;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
import com.fitterapp.personal.entity.profile.ProfileStatus;
import com.fitterapp.personal.entity.profile.RevisionModality;
import com.fitterapp.personal.entity.profile.RevisionServiceArea;
import com.fitterapp.personal.entity.profile.RevisionServiceMode;
import com.fitterapp.personal.entity.service.PriceUnit;
import com.fitterapp.personal.entity.service.ServiceMode;
import com.fitterapp.personal.service.query.AdminProfileDetails;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.entity.UserStatus;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminProfileMapperTests {

  @Mock private Profile profile;
  @Mock private ProfileRevision revision;
  @Mock private ProfileRevision publishedRevision;
  @Mock private User account;
  @Mock private User reviewer;
  @Mock private Cref cref;
  @Mock private RevisionModality modalityLink;
  @Mock private Modality modality;
  @Mock private RevisionServiceMode serviceMode;
  @Mock private RevisionServiceArea serviceArea;

  @Test
  void mapsEveryFieldNeededForAdministrativeReview() {
    UUID profileId = UUID.randomUUID();
    UUID revisionId = UUID.randomUUID();
    UUID publishedRevisionId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID reviewerId = UUID.randomUUID();
    UUID crefId = UUID.randomUUID();
    UUID areaId = UUID.randomUUID();
    OffsetDateTime timestamp = OffsetDateTime.of(2026, 8, 11, 12, 0, 0, 0, ZoneOffset.UTC);

    when(profile.getId()).thenReturn(profileId);
    when(profile.getSlug()).thenReturn("ana-personal");
    when(profile.getStatus()).thenReturn(ProfileStatus.PUBLISHED);
    when(profile.isPublished()).thenReturn(true);
    when(profile.getPublishedRevision()).thenReturn(publishedRevision);
    when(publishedRevision.getId()).thenReturn(publishedRevisionId);
    when(profile.getPublishedAt()).thenReturn(timestamp);
    when(profile.getCreatedAt()).thenReturn(timestamp.minusDays(2));
    when(profile.getUpdatedAt()).thenReturn(timestamp);
    when(profile.getUser()).thenReturn(account);
    when(account.getId()).thenReturn(accountId);
    when(account.getFullName()).thenReturn("Ana Lima");
    when(account.getEmail()).thenReturn("ana@example.com");
    when(account.getPhoneNumber()).thenReturn("+5544999999999");
    when(account.getStatus()).thenReturn(UserStatus.ACTIVE);

    when(revision.getId()).thenReturn(revisionId);
    when(revision.getVersionNumber()).thenReturn(2);
    when(revision.getStatus()).thenReturn(ProfileRevisionStatus.PENDING_REVIEW);
    when(revision.isRequiresReview()).thenReturn(true);
    when(revision.getFullName()).thenReturn("Ana Lima");
    when(revision.getBiography()).thenReturn("Treinamento funcional");
    when(revision.getWhatsapp()).thenReturn("+5544999999999");
    when(revision.getProfileImageKey()).thenReturn("profiles/ana.jpg");
    when(revision.getExperienceStartedYear()).thenReturn((short) 2018);
    when(revision.getCertifications()).thenReturn("Especialização");
    when(revision.getGymsDescription()).thenReturn("Academia Centro");
    when(revision.getStartingPriceCents()).thenReturn(12000);
    when(revision.getPriceUnit()).thenReturn(PriceUnit.PER_SESSION);
    when(revision.getCref()).thenReturn(cref);
    when(revision.getSubmittedAt()).thenReturn(timestamp.minusHours(2));
    when(revision.getReviewedBy()).thenReturn(reviewer);
    when(reviewer.getId()).thenReturn(reviewerId);
    when(revision.getCreatedAt()).thenReturn(timestamp.minusDays(1));
    when(revision.getUpdatedAt()).thenReturn(timestamp.minusHours(2));

    when(cref.getId()).thenReturn(crefId);
    when(cref.getRegistrationCode()).thenReturn("012345-G/PR");
    when(cref.getDocumentImageKey()).thenReturn("crefs/ana.pdf");
    when(cref.getStatus()).thenReturn(CrefStatus.PENDING_REVIEW);

    when(modalityLink.getModality()).thenReturn(modality);
    when(modality.getId()).thenReturn((short) 1);
    when(modality.getName()).thenReturn("Musculação");
    when(modality.getSlug()).thenReturn("musculacao");
    when(modality.isActive()).thenReturn(true);
    when(serviceMode.getServiceMode()).thenReturn(ServiceMode.IN_PERSON);
    when(serviceArea.getId()).thenReturn(areaId);
    when(serviceArea.getCity()).thenReturn("Umuarama");
    when(serviceArea.getStateCode()).thenReturn("PR");
    when(serviceArea.getNeighborhood()).thenReturn("Centro");
    when(serviceArea.getDescription()).thenReturn("Até 5 km");

    var result =
        new AdminProfileMapper()
            .toDetail(
                new AdminProfileDetails(
                    profile,
                    revision,
                    List.of(modalityLink),
                    List.of(serviceMode),
                    List.of(serviceArea)));

    assertThat(result.profileId()).isEqualTo(profileId);
    assertThat(result.account().email()).isEqualTo("ana@example.com");
    assertThat(result.revision().revisionId()).isEqualTo(revisionId);
    assertThat(result.revision().cref().registrationCode()).isEqualTo("012345-G/PR");
    assertThat(result.revision().modalities())
        .singleElement()
        .satisfies(
            item -> {
              assertThat(item.name()).isEqualTo("Musculação");
              assertThat(item.active()).isTrue();
            });
    assertThat(result.revision().serviceModes()).containsExactly(ServiceMode.IN_PERSON);
    assertThat(result.revision().serviceAreas())
        .singleElement()
        .satisfies(
            area -> {
              assertThat(area.id()).isEqualTo(areaId);
              assertThat(area.city()).isEqualTo("Umuarama");
            });
    assertThat(result.revision().startingPriceCents()).isEqualTo(12000);
    assertThat(result.revision().reviewedByUserId()).isEqualTo(reviewerId);
  }

  @Test
  void mapsAndSerializesMissingCrefAsNull() {
    when(profile.getUser()).thenReturn(account);
    when(revision.getCref()).thenReturn(null);

    var result =
        new AdminProfileMapper()
            .toDetail(
                new AdminProfileDetails(profile, revision, List.of(), List.of(), List.of()));
    var json = new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(result);

    assertThat(result.revision().cref()).isNull();
    assertThat(json.at("/revision/cref").isNull()).isTrue();
  }
}
