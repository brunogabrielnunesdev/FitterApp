package com.fitterapp.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRoleId implements Serializable {

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "role_id")
  private Short roleId;

  public UserRoleId(UUID userId, Short roleId) {
    this.userId = Objects.requireNonNull(userId);
    this.roleId = Objects.requireNonNull(roleId);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof UserRoleId that)) {
      return false;
    }
    return Objects.equals(userId, that.userId) && Objects.equals(roleId, that.roleId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, roleId);
  }
}
