package com.fitterapp.user.service.query;

import com.fitterapp.user.exception.UserNotFoundException;
import com.fitterapp.user.repository.UserRepository;
import com.fitterapp.user.repository.UserRoleRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAdminUserService {
  private final UserRepository users;
  private final UserRoleRepository userRoles;

  @Transactional(readOnly = true)
  public AdminUserDetails get(UUID userId) {
    var user = users.findById(userId).orElseThrow(UserNotFoundException::new);
    return new AdminUserDetails(user, userRoles.findAllByUserId(userId));
  }
}
