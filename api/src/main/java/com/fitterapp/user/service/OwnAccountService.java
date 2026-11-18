package com.fitterapp.user.service;

import com.fitterapp.user.entity.RoleName;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.entity.UserStatus;
import com.fitterapp.user.repository.UserRepository;
import com.fitterapp.user.repository.UserRoleRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OwnAccountService {
  private final UserRepository users;
  private final UserRoleRepository roles;
  private final Clock clock;

  @Transactional(readOnly = true)
  public Account get(UUID userId) {
    return view(find(userId));
  }

  @Transactional
  public Account update(UUID userId, String fullName, String phoneNumber) {
    User user = find(userId);
    user.updateBasicDetails(
        fullName.trim().replaceAll("\\s+", " "), phoneNumber.trim(), OffsetDateTime.now(clock));
    return view(user);
  }

  private User find(UUID userId) {
    return users
        .findById(userId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));
  }

  private Account view(User user) {
    List<RoleName> userRoles =
        roles.findAllByUserId(user.getId()).stream().map(role -> role.getRole().getName()).toList();
    return new Account(
        user.getId(),
        user.getFullName(),
        user.getEmail(),
        user.getPhoneNumber(),
        user.getStatus(),
        userRoles);
  }

  public record Account(
      UUID userId,
      String fullName,
      String email,
      String phoneNumber,
      UserStatus status,
      List<RoleName> roles) {}
}
