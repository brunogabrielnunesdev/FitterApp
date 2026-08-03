package com.fitterapp.personal.repository;

import com.fitterapp.personal.entity.profile.RevisionModality;
import com.fitterapp.personal.entity.profile.RevisionModalityId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevisionModalityRepository
    extends JpaRepository<RevisionModality, RevisionModalityId> {

  void deleteByRevisionId(UUID revisionId);

  long countByIdRevisionId(UUID revisionId);

  List<RevisionModality> findAllByRevisionIdOrderByModalityNameAsc(UUID revisionId);
}
