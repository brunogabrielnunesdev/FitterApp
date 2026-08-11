package com.fitterapp.analytics.repository;

import com.fitterapp.analytics.entity.event.ProfileViewEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileViewEventRepository extends JpaRepository<ProfileViewEvent, UUID> {}
