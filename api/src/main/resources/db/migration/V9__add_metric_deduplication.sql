ALTER TABLE search_events
    ADD COLUMN unique_event BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN idempotency_key_hash VARCHAR(64);

ALTER TABLE profile_view_events
    ADD COLUMN unique_event BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN idempotency_key_hash VARCHAR(64);

ALTER TABLE contact_events
    ADD COLUMN unique_event BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN idempotency_key_hash VARCHAR(64);

ALTER TABLE search_events
    ADD CONSTRAINT ck_search_events_idempotency_hash CHECK (
        idempotency_key_hash IS NULL OR idempotency_key_hash ~ '^[0-9a-f]{64}$'
    );

ALTER TABLE profile_view_events
    ADD CONSTRAINT ck_profile_view_events_idempotency_hash CHECK (
        idempotency_key_hash IS NULL OR idempotency_key_hash ~ '^[0-9a-f]{64}$'
    );

ALTER TABLE contact_events
    ADD CONSTRAINT ck_contact_events_idempotency_hash CHECK (
        idempotency_key_hash IS NULL OR idempotency_key_hash ~ '^[0-9a-f]{64}$'
    );

CREATE TABLE metric_idempotency_keys (
    event_type VARCHAR(30) NOT NULL,
    actor_hash CHAR(64) NOT NULL,
    idempotency_key_hash CHAR(64) NOT NULL,
    claimed_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_metric_idempotency_keys
        PRIMARY KEY (event_type, actor_hash, idempotency_key_hash),
    CONSTRAINT ck_metric_idempotency_keys_type CHECK (
        event_type IN ('SEARCH', 'PROFILE_VIEW', 'WHATSAPP_CONTACT')
    ),
    CONSTRAINT ck_metric_idempotency_keys_actor_hash CHECK (
        actor_hash ~ '^[0-9a-f]{64}$'
    ),
    CONSTRAINT ck_metric_idempotency_keys_key_hash CHECK (
        idempotency_key_hash ~ '^[0-9a-f]{64}$'
    )
);

CREATE TABLE metric_unique_states (
    event_type VARCHAR(30) NOT NULL,
    fingerprint_hash CHAR(64) NOT NULL,
    last_unique_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_metric_unique_states
        PRIMARY KEY (event_type, fingerprint_hash),
    CONSTRAINT ck_metric_unique_states_type CHECK (
        event_type IN ('SEARCH', 'PROFILE_VIEW', 'WHATSAPP_CONTACT')
    ),
    CONSTRAINT ck_metric_unique_states_fingerprint_hash CHECK (
        fingerprint_hash ~ '^[0-9a-f]{64}$'
    )
);

CREATE INDEX ix_search_events_unique_occurred_at
    ON search_events (occurred_at DESC) WHERE unique_event;

CREATE INDEX ix_profile_view_events_unique_personal
    ON profile_view_events (personal_profile_id, occurred_at DESC)
    WHERE unique_event AND personal_profile_id IS NOT NULL;

CREATE INDEX ix_contact_events_unique_personal
    ON contact_events (personal_profile_id, occurred_at DESC)
    WHERE unique_event AND personal_profile_id IS NOT NULL;
