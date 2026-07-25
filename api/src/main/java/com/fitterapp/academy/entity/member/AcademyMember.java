package com.fitterapp.academy.entity.member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fitterapp.academy.entity.profile.AcademyProfile;
import java.time.OffsetDateTime;

import com.fitterapp.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "academy_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademyMember {

    @EmbeddedId
    private AcademyMemberId id;

    @MapsId("academyId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academy_id", nullable = false)
    private AcademyProfile academy;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_role", nullable = false, length = 20)
    private AcademyMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcademyMemberStatus status;

    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt;

    @Column(name = "deactivated_at")
    private OffsetDateTime deactivatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;


    public static AcademyMember active(
            AcademyProfile academy,
            User user,
            AcademyMemberRole role,
            OffsetDateTime joinedAt) {
        AcademyMember member = new AcademyMember();
        member.id = new AcademyMemberId(academy.getId(), user.getId());
        member.academy = academy;
        member.user = user;
        member.role = role;
        member.status = AcademyMemberStatus.ACTIVE;
        member.joinedAt = joinedAt;
        member.createdAt = joinedAt;
        member.updatedAt = joinedAt;
        return member;
    }

    public void changeRole(AcademyMemberRole role, OffsetDateTime updatedAt) {
        this.role = role;
        this.updatedAt = updatedAt;
    }

    public void deactivate(OffsetDateTime deactivatedAt) {
        status = AcademyMemberStatus.INACTIVE;
        this.deactivatedAt = deactivatedAt;
        updatedAt = deactivatedAt;
    }









}
