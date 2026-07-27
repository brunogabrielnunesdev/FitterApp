package com.fitterapp.user.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRole {

    @EmbeddedId
    private UserRoleId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("roleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "granted_at", nullable = false, updatable = false)
    private OffsetDateTime grantedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by")
    private User grantedBy;


    public static UserRole grantedBySystem(User user, Role role, OffsetDateTime grantedAt) {
        return granted(user, role, null, grantedAt);
    }

    public static UserRole granted(User user, Role role, User grantedBy, OffsetDateTime grantedAt) {
        UserRole userRole = new UserRole();
        userRole.id = new UserRoleId(user.getId(), role.getId());
        userRole.user = user;
        userRole.role = role;
        userRole.grantedAt = grantedAt;
        userRole.grantedBy = grantedBy;
        return userRole;
    }





}
