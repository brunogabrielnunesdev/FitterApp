package com.fitterapp.analytics.repository;

import com.fitterapp.analytics.entity.event.SearchEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchEventRepository extends JpaRepository<SearchEvent, UUID> {}
