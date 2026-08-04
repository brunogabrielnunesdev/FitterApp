package com.fitterapp.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

  @Id @UuidGenerator private UUID id;

  @Column(name = "full_name", nullable = false, length = 120)
  private String fullName;

  @Column(nullable = false, unique = true, length = 254)
  private String email;

  @Column(name = "phone_number", nullable = false, length = 20)
  private String phoneNumber;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private UserStatus status;

  @Column(name = "email_verified_at")
  private OffsetDateTime emailVerifiedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  public static User pendingRegistration(
      String fullName,
      String email,
      String phoneNumber,
      String passwordHash,
      OffsetDateTime createdAt) {
    User user = new User();
    user.fullName = fullName;
    user.email = email;
    user.phoneNumber = phoneNumber;
    user.passwordHash = passwordHash;
    user.status = UserStatus.PENDING_VERIFICATION;
    user.createdAt = createdAt;
    user.updatedAt = createdAt;
    return user;
  }

  public void confirmEmail(OffsetDateTime confirmedAt) {
    emailVerifiedAt = confirmedAt;
    status = UserStatus.ACTIVE;
    updatedAt = confirmedAt;
  }

  public void block(OffsetDateTime blockedAt) {
    status = UserStatus.BLOCKED;
    updatedAt = blockedAt;
  }

  public void changePassword(String newPasswordHash, OffsetDateTime changedAt) {
    passwordHash = newPasswordHash;
    updatedAt = changedAt;
  }
}
