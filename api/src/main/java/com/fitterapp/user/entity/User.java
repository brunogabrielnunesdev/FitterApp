package com.fitterapp.user.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @UuidGenerator
    private UUID id;

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

    protected User() {
    }

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

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserStatus getStatus() {
        return status;
    }

    public OffsetDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
