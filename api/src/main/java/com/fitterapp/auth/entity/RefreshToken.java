package com.fitterapp.auth.entity;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import com.fitterapp.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by_id")
    private RefreshToken replacedBy;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "ip_address", columnDefinition = "inet")
    private InetAddress ipAddress;

    protected RefreshToken() {
    }

    public static RefreshToken issue(
            User user,
            UUID familyId,
            String tokenHash,
            OffsetDateTime createdAt,
            OffsetDateTime expiresAt,
            String userAgent,
            InetAddress ipAddress) {
        RefreshToken token = new RefreshToken();
        token.id = UUID.randomUUID();
        token.user = user;
        token.familyId = familyId;
        token.tokenHash = tokenHash;
        token.createdAt = createdAt;
        token.expiresAt = expiresAt;
        token.userAgent = userAgent;
        token.ipAddress = ipAddress;
        return token;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public OffsetDateTime getRevokedAt() {
        return revokedAt;
    }

    public RefreshToken getReplacedBy() {
        return replacedBy;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }
}
