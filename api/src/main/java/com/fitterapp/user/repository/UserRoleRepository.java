package com.fitterapp.user.repository;

import com.fitterapp.user.entity.UserRole;
import com.fitterapp.user.entity.UserRoleId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

  @EntityGraph(attributePaths = "role")
  List<UserRole> findAllByUserId(UUID userId);
}
