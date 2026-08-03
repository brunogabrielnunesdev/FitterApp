package com.fitterapp.personal.repository;

import com.fitterapp.personal.entity.profile.RevisionServiceMode;
import com.fitterapp.personal.entity.profile.RevisionServiceModeId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevisionServiceModeRepository
    extends JpaRepository<RevisionServiceMode, RevisionServiceModeId> {

  void deleteByRevisionId(UUID revisionId);

  long countByIdRevisionId(UUID revisionId);

  List<RevisionServiceMode> findAllByRevisionIdOrderByIdServiceModeAsc(UUID revisionId);
}
