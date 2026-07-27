package com.fitterapp.personal.repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileStatus;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    boolean existsByUserId(UUID userId);

    boolean existsBySlug(String slug);

    Optional<Profile> findByUserId(UUID userId);

    Optional<Profile> findByIdAndUserId(UUID id, UUID userId);

    List<Profile> findAllByStatusOrderByUpdatedAtAsc(ProfileStatus status);
}
