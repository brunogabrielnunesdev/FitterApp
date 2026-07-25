package com.fitterapp.academy.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AcademyMemberId implements Serializable {

    @Column(name = "academy_id")
    private UUID academyId;

    @Column(name = "user_id")
    private UUID userId;

    protected AcademyMemberId() {
    }

    public AcademyMemberId(UUID academyId, UUID userId) {
        this.academyId = Objects.requireNonNull(academyId);
        this.userId = Objects.requireNonNull(userId);
    }

    public UUID getAcademyId() {
        return academyId;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AcademyMemberId that)) {
            return false;
        }
        return Objects.equals(academyId, that.academyId)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(academyId, userId);
    }
}
