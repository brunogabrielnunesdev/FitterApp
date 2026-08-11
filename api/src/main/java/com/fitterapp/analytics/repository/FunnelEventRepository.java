package com.fitterapp.analytics.repository;

import com.fitterapp.analytics.entity.event.FunnelEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FunnelEventRepository extends JpaRepository<FunnelEvent, UUID> {}
