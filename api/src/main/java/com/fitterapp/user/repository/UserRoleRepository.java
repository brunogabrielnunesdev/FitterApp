package com.fitterapp.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitterapp.user.entity.UserRole;
import com.fitterapp.user.entity.UserRoleId;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
}
