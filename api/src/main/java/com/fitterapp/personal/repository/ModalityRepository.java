package com.fitterapp.personal.repository;

import com.fitterapp.personal.entity.modality.Modality;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModalityRepository extends JpaRepository<Modality, Short> {

  List<Modality> findAllByIdInAndActiveTrue(Collection<Short> ids);

  List<Modality> findAllByActiveTrueOrderByNameAsc();
}
