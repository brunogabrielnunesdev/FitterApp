package com.fitterapp.moderation.repository;

import com.fitterapp.moderation.entity.suspension.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileSuspensionRepository extends JpaRepository<ProfileSuspension, UUID> {
  Optional<ProfileSuspension> findFirstByPersonalProfileIdAndStatusOrderBySuspendedAtDesc(
      UUID id, SuspensionStatus status);
}
