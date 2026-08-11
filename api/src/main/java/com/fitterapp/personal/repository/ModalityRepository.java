package com.fitterapp.personal.repository;

import com.fitterapp.personal.entity.modality.Modality;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModalityRepository extends JpaRepository<Modality, Short> {

  List<Modality> findAllByIdInAndActiveTrue(Collection<Short> ids);

  List<Modality> findAllByActiveTrueOrderByNameAsc();

  List<Modality> findAllByOrderByNameAsc();

  boolean existsByNameIgnoreCase(String name);

  boolean existsByNameIgnoreCaseAndIdNot(String name, Short id);

  boolean existsBySlug(String slug);

  boolean existsBySlugAndIdNot(String slug, Short id);
}
