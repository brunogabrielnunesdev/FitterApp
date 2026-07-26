CREATE TABLE admin_audit_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID NOT NULL,
    action VARCHAR(80) NOT NULL,
    target_type VARCHAR(30) NOT NULL,
    target_id UUID NOT NULL,
    reason VARCHAR(1500),
    report_id UUID,
    previous_state JSONB,
    new_state JSONB,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_admin_audit_logs_actor FOREIGN KEY (actor_user_id)
        REFERENCES users (id),
    CONSTRAINT fk_admin_audit_logs_report FOREIGN KEY (report_id)
        REFERENCES moderation_reports (id) ON DELETE SET NULL,
    CONSTRAINT ck_admin_audit_logs_action CHECK (
        btrim(action) <> ''
    ),
    CONSTRAINT ck_admin_audit_logs_target_type CHECK (
        target_type IN (
            'USER',
            'PERSONAL_PROFILE',
            'ACADEMY_PROFILE',
            'REPORT',
            'SUSPENSION',
            'ACCOUNT_BLOCK',
            'BLACKLIST_ENTRY',
            'MODALITY',
            'ROLE_ASSIGNMENT'
        )
    ),
    CONSTRAINT ck_admin_audit_logs_reason CHECK (
        reason IS NULL OR btrim(reason) <> ''
    ),
    CONSTRAINT ck_admin_audit_logs_previous_state CHECK (
        previous_state IS NULL OR jsonb_typeof(previous_state) = 'object'
    ),
    CONSTRAINT ck_admin_audit_logs_new_state CHECK (
        new_state IS NULL OR jsonb_typeof(new_state) = 'object'
    )
);

CREATE TABLE search_events (
    id UUID PRIMARY KEY,
    user_id UUID,
    source VARCHAR(20) NOT NULL,
    search_term VARCHAR(120),
    filters JSONB NOT NULL DEFAULT '{}'::jsonb,
    result_count INTEGER NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_search_events_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_search_events_source CHECK (
        source IN ('MOBILE_APP', 'ADMIN_WEB', 'PUBLIC_WEB')
    ),
    CONSTRAINT ck_search_events_search_term CHECK (
        search_term IS NULL OR btrim(search_term) <> ''
    ),
    CONSTRAINT ck_search_events_filters CHECK (
        jsonb_typeof(filters) = 'object'
    ),
    CONSTRAINT ck_search_events_result_count CHECK (
        result_count >= 0
    )
);

CREATE TABLE profile_view_events (
    id UUID PRIMARY KEY,
    viewer_user_id UUID,
    personal_profile_id UUID,
    academy_profile_id UUID,
    source VARCHAR(20) NOT NULL,
    city VARCHAR(100),
    occurred_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_profile_view_events_viewer FOREIGN KEY (viewer_user_id)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_profile_view_events_personal FOREIGN KEY (personal_profile_id)
        REFERENCES personal_profiles (id),
    CONSTRAINT fk_profile_view_events_academy FOREIGN KEY (academy_profile_id)
        REFERENCES academy_profiles (id),
    CONSTRAINT ck_profile_view_events_target CHECK (
        (personal_profile_id IS NOT NULL AND academy_profile_id IS NULL)
        OR (personal_profile_id IS NULL AND academy_profile_id IS NOT NULL)
    ),
    CONSTRAINT ck_profile_view_events_source CHECK (
        source IN ('MOBILE_APP', 'ADMIN_WEB', 'PUBLIC_WEB')
    ),
    CONSTRAINT ck_profile_view_events_city CHECK (
        city IS NULL OR btrim(city) <> ''
    )
);

CREATE TABLE contact_events (
    id UUID PRIMARY KEY,
    user_id UUID,
    personal_profile_id UUID,
    academy_profile_id UUID,
    source VARCHAR(20) NOT NULL,
    city VARCHAR(100),
    occurred_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_contact_events_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_contact_events_personal FOREIGN KEY (personal_profile_id)
        REFERENCES personal_profiles (id),
    CONSTRAINT fk_contact_events_academy FOREIGN KEY (academy_profile_id)
        REFERENCES academy_profiles (id),
    CONSTRAINT ck_contact_events_target CHECK (
        (personal_profile_id IS NOT NULL AND academy_profile_id IS NULL)
        OR (personal_profile_id IS NULL AND academy_profile_id IS NOT NULL)
    ),
    CONSTRAINT ck_contact_events_source CHECK (
        source IN ('MOBILE_APP', 'ADMIN_WEB', 'PUBLIC_WEB')
    ),
    CONSTRAINT ck_contact_events_city CHECK (
        city IS NULL OR btrim(city) <> ''
    )
);

CREATE TABLE app_access_events (
    id UUID PRIMARY KEY,
    user_id UUID,
    event_type VARCHAR(20) NOT NULL,
    source VARCHAR(20) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_app_access_events_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_app_access_events_type CHECK (
        event_type IN ('OPENED', 'RETURNED')
    ),
    CONSTRAINT ck_app_access_events_source CHECK (
        source IN ('MOBILE_APP', 'ADMIN_WEB', 'PUBLIC_WEB')
    )
);

CREATE FUNCTION prevent_admin_audit_log_mutation()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'admin_audit_logs are immutable';
END;
$$;

CREATE TRIGGER trg_admin_audit_logs_immutable
    BEFORE UPDATE OR DELETE ON admin_audit_logs
    FOR EACH ROW
    EXECUTE FUNCTION prevent_admin_audit_log_mutation();

CREATE INDEX ix_admin_audit_logs_target
    ON admin_audit_logs (target_type, target_id, created_at DESC);

CREATE INDEX ix_admin_audit_logs_actor
    ON admin_audit_logs (actor_user_id, created_at DESC);

CREATE INDEX ix_admin_audit_logs_report
    ON admin_audit_logs (report_id)
    WHERE report_id IS NOT NULL;

CREATE INDEX ix_search_events_occurred_at
    ON search_events (occurred_at DESC);

CREATE INDEX ix_search_events_user
    ON search_events (user_id, occurred_at DESC)
    WHERE user_id IS NOT NULL;

CREATE INDEX ix_profile_view_events_personal
    ON profile_view_events (personal_profile_id, occurred_at DESC)
    WHERE personal_profile_id IS NOT NULL;

CREATE INDEX ix_profile_view_events_academy
    ON profile_view_events (academy_profile_id, occurred_at DESC)
    WHERE academy_profile_id IS NOT NULL;

CREATE INDEX ix_contact_events_personal
    ON contact_events (personal_profile_id, occurred_at DESC)
    WHERE personal_profile_id IS NOT NULL;

CREATE INDEX ix_contact_events_academy
    ON contact_events (academy_profile_id, occurred_at DESC)
    WHERE academy_profile_id IS NOT NULL;

CREATE INDEX ix_contact_events_city
    ON contact_events (lower(city), occurred_at DESC)
    WHERE city IS NOT NULL;

CREATE INDEX ix_app_access_events_user
    ON app_access_events (user_id, occurred_at DESC)
    WHERE user_id IS NOT NULL;

CREATE INDEX ix_app_access_events_type
    ON app_access_events (event_type, occurred_at DESC);
