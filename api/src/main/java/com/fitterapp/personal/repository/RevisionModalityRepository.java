package com.fitterapp.personal.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitterapp.personal.entity.profile.RevisionModality;
import com.fitterapp.personal.entity.profile.RevisionModalityId;

public interface RevisionModalityRepository extends JpaRepository<RevisionModality, RevisionModalityId> {

    void deleteByRevisionId(UUID revisionId);
}
