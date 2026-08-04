package com.fitterapp.personal.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.entity.profile.ProfileStatus;
import com.fitterapp.user.entity.User;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class ProfilePublishedRevisionLifecycleTests {
  @Test
  void keepsPublishedStatusWhileNewRevisionIsReviewedAndRejected() {
    OffsetDateTime now = OffsetDateTime.parse("2026-08-04T18:00:00Z");
    User user = User.pendingRegistration("Bruno", "bruno@test.com", "+5544999999999", "hash", now.minusDays(3));
    Profile profile = Profile.draft("Bruno", "bruno", now.minusDays(3));
    profile.linkUser(user, now.minusDays(3));
    ProfileRevision published = ProfileRevision.draft(profile, 1, user, true, now.minusDays(3));
    published.submit(now.minusDays(2));
    published.approve(user, now.minusDays(1));
    profile.publish(published, now.minusDays(1));
    ProfileRevision draft = ProfileRevision.draftFrom(profile, 2, user, published, now);
    profile.setCurrentRevision(draft, now);

    draft.submit(now.plusMinutes(1));
    profile.submitForReview(now.plusMinutes(1));
    assertThat(profile.getStatus()).isEqualTo(ProfileStatus.PUBLISHED);
    draft.reject(user, "Ajustar biografia", now.plusMinutes(2));
    profile.reject(now.plusMinutes(2));
    assertThat(profile.getStatus()).isEqualTo(ProfileStatus.PUBLISHED);
    assertThat(profile.getPublishedRevision()).isSameAs(published);
  }
}
