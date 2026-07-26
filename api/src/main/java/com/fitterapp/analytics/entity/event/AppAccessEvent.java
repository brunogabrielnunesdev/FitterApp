package com.fitterapp.analytics.entity.event;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.fitterapp.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app_access_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppAccessEvent {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private AppAccessEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventSource source;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private OffsetDateTime occurredAt;

    public static AppAccessEvent opened(
            User user,
            EventSource source,
            OffsetDateTime occurredAt) {
        return record(user, AppAccessEventType.OPENED, source, occurredAt);
    }

    public static AppAccessEvent returned(
            User user,
            EventSource source,
            OffsetDateTime occurredAt) {
        return record(user, AppAccessEventType.RETURNED, source, occurredAt);
    }

    private static AppAccessEvent record(
            User user,
            AppAccessEventType eventType,
            EventSource source,
            OffsetDateTime occurredAt) {
        AppAccessEvent event = new AppAccessEvent();
        event.user = user;
        event.eventType = eventType;
        event.source = source;
        event.occurredAt = occurredAt;
        return event;
    }
}
