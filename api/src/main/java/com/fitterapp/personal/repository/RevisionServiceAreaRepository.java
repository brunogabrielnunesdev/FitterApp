package com.fitterapp.personal.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitterapp.personal.entity.profile.RevisionServiceArea;

public interface RevisionServiceAreaRepository extends JpaRepository<RevisionServiceArea, UUID> {

    void deleteByRevisionId(UUID revisionId);

    long countByRevisionId(UUID revisionId);
}
