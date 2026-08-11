CREATE TABLE funnel_events (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    personal_profile_id UUID,
    event_type VARCHAR(30) NOT NULL,
    source VARCHAR(20) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_funnel_events_user FOREIGN KEY (user_id)
        REFERENCES users (id),
    CONSTRAINT fk_funnel_events_personal FOREIGN KEY (personal_profile_id)
        REFERENCES personal_profiles (id),
    CONSTRAINT ck_funnel_events_type CHECK (
        event_type IN ('ACCOUNT_COMPLETED', 'PROFILE_STARTED', 'PROFILE_SUBMITTED')
    ),
    CONSTRAINT ck_funnel_events_source CHECK (
        source IN ('MOBILE_APP', 'ADMIN_WEB', 'PUBLIC_WEB')
    ),
    CONSTRAINT ck_funnel_events_target CHECK (
        (event_type = 'ACCOUNT_COMPLETED' AND personal_profile_id IS NULL)
        OR (event_type IN ('PROFILE_STARTED', 'PROFILE_SUBMITTED')
            AND personal_profile_id IS NOT NULL)
    )
);

CREATE INDEX ix_funnel_events_type_occurred_at
    ON funnel_events (event_type, occurred_at DESC);

CREATE INDEX ix_funnel_events_user_occurred_at
    ON funnel_events (user_id, occurred_at DESC);

CREATE INDEX ix_funnel_events_personal_occurred_at
    ON funnel_events (personal_profile_id, occurred_at DESC)
    WHERE personal_profile_id IS NOT NULL;
