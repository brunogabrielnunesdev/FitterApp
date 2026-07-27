package com.fitterapp.personal.service.review;

import java.time.Clock;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitterapp.auth.exception.RoleNotConfiguredException;
import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
import com.fitterapp.personal.exception.ProfileNotFoundException;
import com.fitterapp.personal.exception.ProfileNotPendingReviewException;
import com.fitterapp.personal.exception.ReviewReasonRequiredException;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.user.entity.RoleName;
import com.fitterapp.user.entity.UserRole;
import com.fitterapp.user.entity.UserRoleId;
import com.fitterapp.user.repository.RoleRepository;
import com.fitterapp.user.repository.UserRepository;
import com.fitterapp.user.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewProfileService {
    private final ProfileRepository profiles; private final UserRepository users;
    private final RoleRepository roles; private final UserRoleRepository userRoles; private final Clock clock;

    @Transactional
    public ReviewProfileResult approve(ApproveProfileCommand command) {
        var profile = profiles.findById(command.profileId()).orElseThrow(ProfileNotFoundException::new);
        var revision = pending(profile.getCurrentRevision()); var admin = users.findById(command.adminUserId()).orElseThrow();
        var personalRole = roles.findByName(RoleName.PERSONAL).orElseThrow(() -> new RoleNotConfiguredException(RoleName.PERSONAL));
        OffsetDateTime now = OffsetDateTime.now(clock); revision.approve(admin, now); profile.approve(now);
        var id = new UserRoleId(profile.getUser().getId(), personalRole.getId());
        if (!userRoles.existsById(id)) userRoles.save(UserRole.granted(profile.getUser(), personalRole, admin, now));
        return new ReviewProfileResult(profile.getId(), revision.getId());
    }
    @Transactional
    public ReviewProfileResult reject(RejectProfileCommand command) {
        if (command.reason() == null || command.reason().isBlank()) throw new ReviewReasonRequiredException();
        var profile = profiles.findById(command.profileId()).orElseThrow(ProfileNotFoundException::new);
        var revision = pending(profile.getCurrentRevision()); var admin = users.findById(command.adminUserId()).orElseThrow();
        OffsetDateTime now = OffsetDateTime.now(clock); revision.reject(admin, command.reason().trim(), now); profile.reject(now);
        return new ReviewProfileResult(profile.getId(), revision.getId());
    }
    private com.fitterapp.personal.entity.profile.ProfileRevision pending(com.fitterapp.personal.entity.profile.ProfileRevision revision) {
        if (revision == null || revision.getStatus() != ProfileRevisionStatus.PENDING_REVIEW) throw new ProfileNotPendingReviewException();
        return revision;
    }
}
