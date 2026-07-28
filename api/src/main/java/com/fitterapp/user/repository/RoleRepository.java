package com.fitterapp.user.repository;

import com.fitterapp.user.entity.Role;
import com.fitterapp.user.entity.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Short> {

  Optional<Role> findByName(RoleName name);
}
