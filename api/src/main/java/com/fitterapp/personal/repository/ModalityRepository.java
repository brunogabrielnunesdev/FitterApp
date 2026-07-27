package com.fitterapp.personal.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitterapp.personal.entity.modality.Modality;

public interface ModalityRepository extends JpaRepository<Modality, Short> {

    List<Modality> findAllByIdInAndActiveTrue(Collection<Short> ids);
}
