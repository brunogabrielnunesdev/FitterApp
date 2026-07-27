package com.fitterapp.personal.service.create;

import java.time.Clock;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.exception.ProfileAlreadyExistsException;
import com.fitterapp.personal.exception.ProfileApplicantNotFoundException;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.repository.ProfileRevisionRepository;
import com.fitterapp.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateProfileService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ProfileRevisionRepository profileRevisionRepository;
    private final ProfileSlugGenerator slugGenerator;
    private final Clock clock;

    @Transactional
    public CreateProfileResult create(CreateProfileCommand command) {
        if (profileRepository.existsByUserId(command.userId())) {
            throw new ProfileAlreadyExistsException();
        }

        var user = userRepository.findById(command.userId())
                .orElseThrow(ProfileApplicantNotFoundException::new);
        OffsetDateTime createdAt = OffsetDateTime.now(clock);
        String slug = slugGenerator.generate(user.getFullName(), user.getId());
        Profile profile = Profile.draft(user.getFullName(), slug, createdAt);
        profile.linkUser(user, createdAt);
        profileRepository.save(profile);

        ProfileRevision revision = ProfileRevision.draft(
                profile,
                1,
                user,
                true,
                createdAt);
        profileRevisionRepository.save(revision);
        profile.setCurrentRevision(revision, createdAt);

        return new CreateProfileResult(profile.getId(), revision.getId());
    }
}
