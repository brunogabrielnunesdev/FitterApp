CREATE INDEX ix_personal_profile_revisions_reviewed_at
    ON personal_profile_revisions (reviewed_at DESC, status)
    WHERE reviewed_at IS NOT NULL;

CREATE INDEX ix_profile_view_events_occurred_at
    ON profile_view_events (occurred_at DESC);

CREATE INDEX ix_profile_view_events_unique_occurred_at
    ON profile_view_events (occurred_at DESC)
    WHERE unique_event;

CREATE INDEX ix_contact_events_occurred_at
    ON contact_events (occurred_at DESC);

CREATE INDEX ix_contact_events_unique_occurred_at
    ON contact_events (occurred_at DESC)
    WHERE unique_event;
