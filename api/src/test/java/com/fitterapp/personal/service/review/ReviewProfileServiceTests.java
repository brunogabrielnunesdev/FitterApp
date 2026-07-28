package com.fitterapp.personal.service.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitterapp.personal.entity.profile.*;
import com.fitterapp.personal.exception.*;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.user.entity.*;
import com.fitterapp.user.repository.*;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReviewProfileServiceTests {
  static final Instant NOW = Instant.parse("2026-07-27T21:00:00Z");
  @Mock ProfileRepository profiles;
  @Mock UserRepository users;
  @Mock RoleRepository roles;
  @Mock UserRoleRepository userRoles;

  @Test
  void approvesAndGrantsPersonalRole() {
    F f = f();
    Role role = role();
    when(profiles.findById(f.profileId)).thenReturn(Optional.of(f.profile));
    when(users.findById(f.adminId)).thenReturn(Optional.of(f.admin));
    when(roles.findByName(RoleName.PERSONAL)).thenReturn(Optional.of(role));
    when(userRoles.existsById(any())).thenReturn(false);
    service().approve(new ApproveProfileCommand(f.adminId, f.profileId));
    assertThat(f.revision.getStatus()).isEqualTo(ProfileRevisionStatus.APPROVED);
    verify(userRoles).save(any(UserRole.class));
  }

  @Test
  void rejectsWithReason() {
    F f = f();
    when(profiles.findById(f.profileId)).thenReturn(Optional.of(f.profile));
    when(users.findById(f.adminId)).thenReturn(Optional.of(f.admin));
    service().reject(new RejectProfileCommand(f.adminId, f.profileId, " CREF inválido "));
    assertThat(f.revision.getStatus()).isEqualTo(ProfileRevisionStatus.REJECTED);
    assertThat(f.revision.getRejectionReason()).isEqualTo("CREF inválido");
  }

  @Test
  void requiresReasonBeforeLoadingProfile() {
    assertThatThrownBy(
            () ->
                service()
                    .reject(new RejectProfileCommand(UUID.randomUUID(), UUID.randomUUID(), " ")))
        .isInstanceOf(ReviewReasonRequiredException.class);
    verify(profiles, never()).findById(any());
  }

  private ReviewProfileService service() {
    return new ReviewProfileService(
        profiles, users, roles, userRoles, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  private F f() {
    UUID ownerId = UUID.randomUUID(),
        adminId = UUID.randomUUID(),
        profileId = UUID.randomUUID(),
        revisionId = UUID.randomUUID();
    User owner = user(ownerId), admin = user(adminId);
    Profile p = Profile.draft("Bruno", "bruno", NOW.atOffset(ZoneOffset.UTC));
    ReflectionTestUtils.setField(p, "id", profileId);
    p.linkUser(owner, NOW.atOffset(ZoneOffset.UTC));
    ProfileRevision r = ProfileRevision.draft(p, 1, owner, true, NOW.atOffset(ZoneOffset.UTC));
    ReflectionTestUtils.setField(r, "id", revisionId);
    r.submit(NOW.atOffset(ZoneOffset.UTC));
    p.setCurrentRevision(r, NOW.atOffset(ZoneOffset.UTC));
    return new F(ownerId, adminId, profileId, p, r, admin);
  }

  private User user(UUID id) {
    User u =
        User.pendingRegistration(
            "User", "u" + id + "@x.com", "+5544999999999", "hash", NOW.atOffset(ZoneOffset.UTC));
    ReflectionTestUtils.setField(u, "id", id);
    return u;
  }

  private Role role() {
    Role r = mock(Role.class);
    when(r.getId()).thenReturn((short) 2);
    return r;
  }

  private record F(
      UUID ownerId,
      UUID adminId,
      UUID profileId,
      Profile profile,
      ProfileRevision revision,
      User admin) {}
}
