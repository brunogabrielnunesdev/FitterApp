CREATE TABLE moderation_reports (
    id UUID PRIMARY KEY,
    reporter_user_id UUID NOT NULL,
    personal_profile_id UUID,
    academy_profile_id UUID,
    reason VARCHAR(40) NOT NULL,
    description VARCHAR(1500),
    status VARCHAR(20) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    resolution VARCHAR(30),
    resolution_note VARCHAR(1500),
    reviewed_by UUID,
    reviewed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_moderation_reports_reporter FOREIGN KEY (reporter_user_id)
        REFERENCES users (id),
    CONSTRAINT fk_moderation_reports_personal FOREIGN KEY (personal_profile_id)
        REFERENCES personal_profiles (id),
    CONSTRAINT fk_moderation_reports_academy FOREIGN KEY (academy_profile_id)
        REFERENCES academy_profiles (id),
    CONSTRAINT fk_moderation_reports_reviewer FOREIGN KEY (reviewed_by)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_moderation_reports_target CHECK (
        (personal_profile_id IS NOT NULL AND academy_profile_id IS NULL)
        OR (personal_profile_id IS NULL AND academy_profile_id IS NOT NULL)
    ),
    CONSTRAINT ck_moderation_reports_reason CHECK (
        reason IN (
            'FALSE_INFORMATION',
            'INAPPROPRIATE_CONTENT',
            'FRAUD_OR_SCAM',
            'HARASSMENT',
            'PROFESSIONAL_CREDENTIAL',
            'COMMERCIAL_ISSUE',
            'OTHER'
        )
    ),
    CONSTRAINT ck_moderation_reports_description CHECK (
        description IS NULL OR btrim(description) <> ''
    ),
    CONSTRAINT ck_moderation_reports_other_description CHECK (
        reason <> 'OTHER' OR description IS NOT NULL
    ),
    CONSTRAINT ck_moderation_reports_status CHECK (
        status IN ('OPEN', 'UNDER_REVIEW', 'RESOLVED', 'DISMISSED')
    ),
    CONSTRAINT ck_moderation_reports_priority CHECK (
        priority IN ('NORMAL', 'HIGH', 'CRITICAL')
    ),
    CONSTRAINT ck_moderation_reports_resolution CHECK (
        resolution IS NULL OR resolution IN (
            'NO_ACTION',
            'CORRECTION_REQUESTED',
            'WARNING_ISSUED',
            'PROFILE_SUSPENDED',
            'ACCOUNT_BLOCKED'
        )
    ),
    CONSTRAINT ck_moderation_reports_workflow CHECK (
        (status IN ('OPEN', 'UNDER_REVIEW')
            AND resolution IS NULL
            AND resolution_note IS NULL
            AND reviewed_at IS NULL)
        OR (status = 'RESOLVED'
            AND resolution IS NOT NULL
            AND resolution_note IS NOT NULL
            AND btrim(resolution_note) <> ''
            AND reviewed_by IS NOT NULL
            AND reviewed_at IS NOT NULL)
        OR (status = 'DISMISSED'
            AND resolution = 'NO_ACTION'
            AND resolution_note IS NOT NULL
            AND btrim(resolution_note) <> ''
            AND reviewed_by IS NOT NULL
            AND reviewed_at IS NOT NULL)
    ),
    CONSTRAINT ck_moderation_reports_review_after_creation CHECK (
        reviewed_at IS NULL OR reviewed_at >= created_at
    ),
    CONSTRAINT ck_moderation_reports_updated_after_created CHECK (
        updated_at >= created_at
    )
);

CREATE TABLE moderation_report_evidences (
    id UUID PRIMARY KEY,
    report_id UUID NOT NULL,
    position SMALLINT NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_moderation_report_evidences_position
        UNIQUE (report_id, position),
    CONSTRAINT fk_moderation_report_evidences_report FOREIGN KEY (report_id)
        REFERENCES moderation_reports (id) ON DELETE CASCADE,
    CONSTRAINT ck_moderation_report_evidences_position CHECK (
        position BETWEEN 1 AND 3
    ),
    CONSTRAINT ck_moderation_report_evidences_storage_key CHECK (
        storage_key ~ '^moderation/reports/[0-9a-f-]+/[0-9a-f-]+\.webp$'
        AND storage_key NOT LIKE '%..%'
    )
);

CREATE TABLE profile_suspensions (
    id UUID PRIMARY KEY,
    personal_profile_id UUID,
    academy_profile_id UUID,
    report_id UUID,
    suspended_by UUID NOT NULL,
    reason VARCHAR(1500) NOT NULL,
    previous_status VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    suspended_at TIMESTAMPTZ NOT NULL,
    eligible_for_reactivation_at TIMESTAMPTZ NOT NULL,
    lifted_at TIMESTAMPTZ,
    lifted_by UUID,
    lift_reason VARCHAR(1500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_profile_suspensions_personal FOREIGN KEY (personal_profile_id)
        REFERENCES personal_profiles (id),
    CONSTRAINT fk_profile_suspensions_academy FOREIGN KEY (academy_profile_id)
        REFERENCES academy_profiles (id),
    CONSTRAINT fk_profile_suspensions_report FOREIGN KEY (report_id)
        REFERENCES moderation_reports (id) ON DELETE SET NULL,
    CONSTRAINT fk_profile_suspensions_suspended_by FOREIGN KEY (suspended_by)
        REFERENCES users (id),
    CONSTRAINT fk_profile_suspensions_lifted_by FOREIGN KEY (lifted_by)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_profile_suspensions_target CHECK (
        (personal_profile_id IS NOT NULL AND academy_profile_id IS NULL)
        OR (personal_profile_id IS NULL AND academy_profile_id IS NOT NULL)
    ),
    CONSTRAINT ck_profile_suspensions_reason CHECK (
        btrim(reason) <> ''
    ),
    CONSTRAINT ck_profile_suspensions_previous_status CHECK (
        previous_status IN ('APPROVED', 'PUBLISHED')
    ),
    CONSTRAINT ck_profile_suspensions_status CHECK (
        status IN ('ACTIVE', 'LIFTED')
    ),
    CONSTRAINT ck_profile_suspensions_lifecycle CHECK (
        (status = 'ACTIVE'
            AND lifted_at IS NULL
            AND lifted_by IS NULL
            AND lift_reason IS NULL)
        OR (status = 'LIFTED'
            AND lifted_at IS NOT NULL
            AND lifted_by IS NOT NULL
            AND lift_reason IS NOT NULL
            AND btrim(lift_reason) <> '')
    ),
    CONSTRAINT ck_profile_suspensions_reactivation_date CHECK (
        eligible_for_reactivation_at > suspended_at
    ),
    CONSTRAINT ck_profile_suspensions_lift_after_start CHECK (
        lifted_at IS NULL OR lifted_at >= suspended_at
    ),
    CONSTRAINT ck_profile_suspensions_updated_after_created CHECK (
        updated_at >= created_at
    )
);

CREATE TABLE reactivation_requests (
    id UUID PRIMARY KEY,
    suspension_id UUID NOT NULL,
    requested_by UUID NOT NULL,
    reason VARCHAR(1500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reviewed_by UUID,
    reviewed_at TIMESTAMPTZ,
    review_note VARCHAR(1500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_reactivation_requests_suspension FOREIGN KEY (suspension_id)
        REFERENCES profile_suspensions (id),
    CONSTRAINT fk_reactivation_requests_requested_by FOREIGN KEY (requested_by)
        REFERENCES users (id),
    CONSTRAINT fk_reactivation_requests_reviewed_by FOREIGN KEY (reviewed_by)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_reactivation_requests_reason CHECK (
        btrim(reason) <> ''
    ),
    CONSTRAINT ck_reactivation_requests_status CHECK (
        status IN ('PENDING', 'APPROVED', 'REJECTED')
    ),
    CONSTRAINT ck_reactivation_requests_workflow CHECK (
        (status = 'PENDING'
            AND reviewed_by IS NULL
            AND reviewed_at IS NULL
            AND review_note IS NULL)
        OR (status IN ('APPROVED', 'REJECTED')
            AND reviewed_by IS NOT NULL
            AND reviewed_at IS NOT NULL
            AND review_note IS NOT NULL
            AND btrim(review_note) <> '')
    ),
    CONSTRAINT ck_reactivation_requests_review_after_creation CHECK (
        reviewed_at IS NULL OR reviewed_at >= created_at
    ),
    CONSTRAINT ck_reactivation_requests_updated_after_created CHECK (
        updated_at >= created_at
    )
);

CREATE TABLE account_blocks (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    report_id UUID,
    blocked_by UUID NOT NULL,
    reason VARCHAR(1500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    blocked_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    revoked_by UUID,
    revoke_reason VARCHAR(1500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_account_blocks_user FOREIGN KEY (user_id)
        REFERENCES users (id),
    CONSTRAINT fk_account_blocks_report FOREIGN KEY (report_id)
        REFERENCES moderation_reports (id) ON DELETE SET NULL,
    CONSTRAINT fk_account_blocks_blocked_by FOREIGN KEY (blocked_by)
        REFERENCES users (id),
    CONSTRAINT fk_account_blocks_revoked_by FOREIGN KEY (revoked_by)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_account_blocks_reason CHECK (
        btrim(reason) <> ''
    ),
    CONSTRAINT ck_account_blocks_status CHECK (
        status IN ('ACTIVE', 'REVOKED')
    ),
    CONSTRAINT ck_account_blocks_lifecycle CHECK (
        (status = 'ACTIVE'
            AND revoked_at IS NULL
            AND revoked_by IS NULL
            AND revoke_reason IS NULL)
        OR (status = 'REVOKED'
            AND revoked_at IS NOT NULL
            AND revoked_by IS NOT NULL
            AND revoke_reason IS NOT NULL
            AND btrim(revoke_reason) <> '')
    ),
    CONSTRAINT ck_account_blocks_revoke_after_block CHECK (
        revoked_at IS NULL OR revoked_at >= blocked_at
    ),
    CONSTRAINT ck_account_blocks_updated_after_created CHECK (
        updated_at >= created_at
    )
);

CREATE TABLE blacklist_entries (
    id UUID PRIMARY KEY,
    account_block_id UUID NOT NULL,
    identifier_type VARCHAR(20) NOT NULL,
    identifier_hash CHAR(64) NOT NULL,
    identifier_suffix VARCHAR(20),
    status VARCHAR(20) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    revoked_by UUID,
    revoke_reason VARCHAR(1500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_blacklist_entries_block_identifier
        UNIQUE (account_block_id, identifier_type, identifier_hash),
    CONSTRAINT fk_blacklist_entries_account_block FOREIGN KEY (account_block_id)
        REFERENCES account_blocks (id),
    CONSTRAINT fk_blacklist_entries_revoked_by FOREIGN KEY (revoked_by)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_blacklist_entries_identifier_type CHECK (
        identifier_type IN ('EMAIL', 'PHONE', 'CREF', 'CNPJ')
    ),
    CONSTRAINT ck_blacklist_entries_identifier_hash CHECK (
        identifier_hash ~ '^[0-9a-f]{64}$'
    ),
    CONSTRAINT ck_blacklist_entries_identifier_suffix CHECK (
        identifier_suffix IS NULL OR btrim(identifier_suffix) <> ''
    ),
    CONSTRAINT ck_blacklist_entries_status CHECK (
        status IN ('ACTIVE', 'EXPIRED', 'REVOKED')
    ),
    CONSTRAINT ck_blacklist_entries_lifecycle CHECK (
        (status IN ('ACTIVE', 'EXPIRED')
            AND revoked_at IS NULL
            AND revoked_by IS NULL
            AND revoke_reason IS NULL)
        OR (status = 'REVOKED'
            AND revoked_at IS NOT NULL
            AND revoked_by IS NOT NULL
            AND revoke_reason IS NOT NULL
            AND btrim(revoke_reason) <> '')
    ),
    CONSTRAINT ck_blacklist_entries_expiration CHECK (
        expires_at > created_at
    ),
    CONSTRAINT ck_blacklist_entries_revoke_after_creation CHECK (
        revoked_at IS NULL OR revoked_at >= created_at
    ),
    CONSTRAINT ck_blacklist_entries_updated_after_created CHECK (
        updated_at >= created_at
    )
);

CREATE TABLE moderation_appeals (
    id UUID PRIMARY KEY,
    suspension_id UUID,
    account_block_id UUID,
    appellant_user_id UUID NOT NULL,
    reason VARCHAR(1500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reviewed_by UUID,
    reviewed_at TIMESTAMPTZ,
    review_note VARCHAR(1500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_moderation_appeals_suspension FOREIGN KEY (suspension_id)
        REFERENCES profile_suspensions (id),
    CONSTRAINT fk_moderation_appeals_account_block FOREIGN KEY (account_block_id)
        REFERENCES account_blocks (id),
    CONSTRAINT fk_moderation_appeals_appellant FOREIGN KEY (appellant_user_id)
        REFERENCES users (id),
    CONSTRAINT fk_moderation_appeals_reviewed_by FOREIGN KEY (reviewed_by)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_moderation_appeals_target CHECK (
        (suspension_id IS NOT NULL AND account_block_id IS NULL)
        OR (suspension_id IS NULL AND account_block_id IS NOT NULL)
    ),
    CONSTRAINT ck_moderation_appeals_reason CHECK (
        btrim(reason) <> ''
    ),
    CONSTRAINT ck_moderation_appeals_status CHECK (
        status IN ('PENDING', 'APPROVED', 'REJECTED')
    ),
    CONSTRAINT ck_moderation_appeals_workflow CHECK (
        (status = 'PENDING'
            AND reviewed_by IS NULL
            AND reviewed_at IS NULL
            AND review_note IS NULL)
        OR (status IN ('APPROVED', 'REJECTED')
            AND reviewed_by IS NOT NULL
            AND reviewed_at IS NOT NULL
            AND review_note IS NOT NULL
            AND btrim(review_note) <> '')
    ),
    CONSTRAINT ck_moderation_appeals_review_after_creation CHECK (
        reviewed_at IS NULL OR reviewed_at >= created_at
    ),
    CONSTRAINT ck_moderation_appeals_updated_after_created CHECK (
        updated_at >= created_at
    )
);

CREATE UNIQUE INDEX uk_moderation_reports_open_personal
    ON moderation_reports (reporter_user_id, personal_profile_id, reason)
    WHERE personal_profile_id IS NOT NULL
      AND status IN ('OPEN', 'UNDER_REVIEW');

CREATE UNIQUE INDEX uk_moderation_reports_open_academy
    ON moderation_reports (reporter_user_id, academy_profile_id, reason)
    WHERE academy_profile_id IS NOT NULL
      AND status IN ('OPEN', 'UNDER_REVIEW');

CREATE UNIQUE INDEX uk_profile_suspensions_active_personal
    ON profile_suspensions (personal_profile_id)
    WHERE personal_profile_id IS NOT NULL AND status = 'ACTIVE';

CREATE UNIQUE INDEX uk_profile_suspensions_active_academy
    ON profile_suspensions (academy_profile_id)
    WHERE academy_profile_id IS NOT NULL AND status = 'ACTIVE';

CREATE UNIQUE INDEX uk_reactivation_requests_pending
    ON reactivation_requests (suspension_id)
    WHERE status = 'PENDING';

CREATE UNIQUE INDEX uk_account_blocks_active_user
    ON account_blocks (user_id)
    WHERE status = 'ACTIVE';

CREATE UNIQUE INDEX uk_blacklist_entries_active_identifier
    ON blacklist_entries (identifier_type, identifier_hash)
    WHERE status = 'ACTIVE';

CREATE UNIQUE INDEX uk_moderation_appeals_pending_suspension
    ON moderation_appeals (suspension_id)
    WHERE suspension_id IS NOT NULL AND status = 'PENDING';

CREATE UNIQUE INDEX uk_moderation_appeals_pending_account_block
    ON moderation_appeals (account_block_id)
    WHERE account_block_id IS NOT NULL AND status = 'PENDING';

CREATE INDEX ix_moderation_reports_queue
    ON moderation_reports (priority, created_at)
    WHERE status IN ('OPEN', 'UNDER_REVIEW');

CREATE INDEX ix_moderation_reports_personal
    ON moderation_reports (personal_profile_id, status);

CREATE INDEX ix_moderation_reports_academy
    ON moderation_reports (academy_profile_id, status);

CREATE INDEX ix_profile_suspensions_report
    ON profile_suspensions (report_id);

CREATE INDEX ix_reactivation_requests_queue
    ON reactivation_requests (created_at)
    WHERE status = 'PENDING';

CREATE INDEX ix_account_blocks_report
    ON account_blocks (report_id);

CREATE INDEX ix_blacklist_entries_lookup
    ON blacklist_entries (identifier_type, identifier_hash, status, expires_at);

CREATE INDEX ix_moderation_appeals_queue
    ON moderation_appeals (created_at)
    WHERE status = 'PENDING';
