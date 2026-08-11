package com.fitterapp.analytics.repository;

import com.fitterapp.analytics.entity.event.ContactEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactEventRepository extends JpaRepository<ContactEvent, UUID> {
  long countByUniqueEventTrue();
}
