package com.fitterapp.user.service.query;

import com.fitterapp.user.entity.User;
import com.fitterapp.user.entity.UserRole;
import java.util.List;

public record AdminUserDetails(User user, List<UserRole> roles) {
  public AdminUserDetails {
    roles = List.copyOf(roles);
  }
}
