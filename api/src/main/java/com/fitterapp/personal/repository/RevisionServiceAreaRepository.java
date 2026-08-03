package com.fitterapp.personal.repository;

import com.fitterapp.personal.entity.profile.RevisionServiceArea;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevisionServiceAreaRepository extends JpaRepository<RevisionServiceArea, UUID> {

  void deleteByRevisionId(UUID revisionId);

  long countByRevisionId(UUID revisionId);

  List<RevisionServiceArea> findAllByRevisionIdOrderByCityAscNeighborhoodAsc(UUID revisionId);
}
