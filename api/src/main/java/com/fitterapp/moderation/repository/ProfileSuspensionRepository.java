package com.fitterapp.moderation.repository;

import com.fitterapp.moderation.entity.suspension.ProfileSuspension;
import com.fitterapp.moderation.entity.suspension.SuspensionStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileSuspensionRepository extends JpaRepository<ProfileSuspension, UUID> {
  Optional<ProfileSuspension> findByPersonalProfileIdAndStatus(
      UUID personalProfileId, SuspensionStatus status);
}
