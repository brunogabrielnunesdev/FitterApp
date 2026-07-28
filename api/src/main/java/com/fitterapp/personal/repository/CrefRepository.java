package com.fitterapp.personal.repository;

import com.fitterapp.personal.entity.cref.Cref;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrefRepository extends JpaRepository<Cref, UUID> {

  Optional<Cref> findByPersonalId(UUID personalId);

  Optional<Cref> findByRegistrationCode(String registrationCode);
}
