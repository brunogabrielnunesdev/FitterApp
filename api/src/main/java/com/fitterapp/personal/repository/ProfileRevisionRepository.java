package com.fitterapp.personal.repository;

import com.fitterapp.personal.entity.profile.ProfileRevision;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRevisionRepository extends JpaRepository<ProfileRevision, UUID> {

  Optional<ProfileRevision> findTopByPersonalIdOrderByVersionNumberDesc(UUID personalId);
}
