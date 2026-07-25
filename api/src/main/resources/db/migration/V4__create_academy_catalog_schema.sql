CREATE TABLE academy_profiles (
    id UUID PRIMARY KEY,
    handle VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    current_revision_id UUID,
    published_revision_id UUID,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_academy_profiles_handle UNIQUE (handle),
    CONSTRAINT ck_academy_profiles_handle CHECK (
        handle ~ '^[a-z][a-z0-9_]{2,49}$'
    ),
    CONSTRAINT ck_academy_profiles_status CHECK (
        status IN (
            'DRAFT',
            'PENDING_REVIEW',
            'APPROVED',
            'PUBLISHED',
            'REJECTED',
            'SUSPENDED'
        )
    ),
    CONSTRAINT ck_academy_profiles_publication CHECK (
        (status = 'PUBLISHED'
            AND published_revision_id IS NOT NULL
            AND published_at IS NOT NULL)
        OR (status = 'SUSPENDED'
            AND published_revision_id IS NOT NULL)
        OR (status IN ('DRAFT', 'PENDING_REVIEW', 'APPROVED', 'REJECTED')
            AND published_at IS NULL)
    ),
    CONSTRAINT ck_academy_profiles_updated_after_created CHECK (
        updated_at >= created_at
    )
);

CREATE TABLE academy_cnpjs (
    id UUID PRIMARY KEY,
    academy_id UUID NOT NULL,
    registration_number CHAR(14) NOT NULL,
    status VARCHAR(30) NOT NULL,
    verified_at TIMESTAMPTZ,
    verified_by UUID,
    rejection_reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_academy_cnpjs_registration_number
        UNIQUE (registration_number),
    CONSTRAINT fk_academy_cnpjs_academy FOREIGN KEY (academy_id)
        REFERENCES academy_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_academy_cnpjs_verified_by FOREIGN KEY (verified_by)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_academy_cnpjs_registration_number CHECK (
        registration_number ~ '^[0-9]{14}$'
    ),
    CONSTRAINT ck_academy_cnpjs_status CHECK (
        status IN ('PENDING_REVIEW', 'VERIFIED', 'REJECTED')
    ),
    CONSTRAINT ck_academy_cnpjs_verification CHECK (
        (status = 'PENDING_REVIEW'
            AND verified_at IS NULL
            AND verified_by IS NULL
            AND rejection_reason IS NULL)
        OR (status = 'VERIFIED'
            AND verified_at IS NOT NULL
            AND verified_by IS NOT NULL
            AND rejection_reason IS NULL)
        OR (status = 'REJECTED'
            AND verified_at IS NOT NULL
            AND verified_by IS NOT NULL
            AND btrim(rejection_reason) <> '')
    ),
    CONSTRAINT ck_academy_cnpjs_updated_after_created CHECK (
        updated_at >= created_at
    )
);

CREATE TABLE academy_profile_revisions (
    id UUID PRIMARY KEY,
    academy_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    name VARCHAR(160),
    description VARCHAR(2000),
    whatsapp VARCHAR(20),
    instagram VARCHAR(100),
    logo_image_key VARCHAR(255),
    cnpj_id UUID,
    postal_code CHAR(8),
    street VARCHAR(160),
    street_number VARCHAR(20),
    address_complement VARCHAR(100),
    neighborhood VARCHAR(100),
    city VARCHAR(100),
    state_code CHAR(2),
    status VARCHAR(30) NOT NULL,
    requires_review BOOLEAN NOT NULL,
    rejection_reason VARCHAR(500),
    created_by UUID,
    submitted_at TIMESTAMPTZ,
    reviewed_at TIMESTAMPTZ,
    reviewed_by UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_academy_profile_revisions_version
        UNIQUE (academy_id, version_number),
    CONSTRAINT fk_academy_profile_revisions_academy FOREIGN KEY (academy_id)
        REFERENCES academy_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_academy_profile_revisions_cnpj FOREIGN KEY (cnpj_id)
        REFERENCES academy_cnpjs (id),
    CONSTRAINT fk_academy_profile_revisions_created_by FOREIGN KEY (created_by)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_academy_profile_revisions_reviewed_by FOREIGN KEY (reviewed_by)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_academy_profile_revisions_version_positive CHECK (
        version_number > 0
    ),
    CONSTRAINT ck_academy_profile_revisions_name CHECK (
        name IS NULL OR btrim(name) <> ''
    ),
    CONSTRAINT ck_academy_profile_revisions_description CHECK (
        description IS NULL OR btrim(description) <> ''
    ),
    CONSTRAINT ck_academy_profile_revisions_whatsapp CHECK (
        whatsapp IS NULL OR whatsapp ~ '^\+[1-9][0-9]{7,14}$'
    ),
    CONSTRAINT ck_academy_profile_revisions_instagram CHECK (
        instagram IS NULL OR instagram ~ '^@[A-Za-z0-9._]{1,30}$'
    ),
    CONSTRAINT ck_academy_profile_revisions_logo_key CHECK (
        logo_image_key IS NULL
        OR (
            logo_image_key ~ '^academies/[0-9a-f-]+/[0-9a-f-]+\.webp$'
            AND logo_image_key NOT LIKE '%..%'
        )
    ),
    CONSTRAINT ck_academy_profile_revisions_postal_code CHECK (
        postal_code IS NULL OR postal_code ~ '^[0-9]{8}$'
    ),
    CONSTRAINT ck_academy_profile_revisions_street CHECK (
        street IS NULL OR btrim(street) <> ''
    ),
    CONSTRAINT ck_academy_profile_revisions_street_number CHECK (
        street_number IS NULL OR btrim(street_number) <> ''
    ),
    CONSTRAINT ck_academy_profile_revisions_complement CHECK (
        address_complement IS NULL OR btrim(address_complement) <> ''
    ),
    CONSTRAINT ck_academy_profile_revisions_neighborhood CHECK (
        neighborhood IS NULL OR btrim(neighborhood) <> ''
    ),
    CONSTRAINT ck_academy_profile_revisions_city CHECK (
        city IS NULL OR btrim(city) <> ''
    ),
    CONSTRAINT ck_academy_profile_revisions_state_code CHECK (
        state_code IS NULL OR state_code ~ '^[A-Z]{2}$'
    ),
    CONSTRAINT ck_academy_profile_revisions_address_group CHECK (
        (postal_code IS NULL
            AND street IS NULL
            AND street_number IS NULL
            AND address_complement IS NULL
            AND neighborhood IS NULL
            AND city IS NULL
            AND state_code IS NULL)
        OR (postal_code IS NOT NULL
            AND street IS NOT NULL
            AND street_number IS NOT NULL
            AND neighborhood IS NOT NULL
            AND city IS NOT NULL
            AND state_code IS NOT NULL)
    ),
    CONSTRAINT ck_academy_profile_revisions_status CHECK (
        status IN ('DRAFT', 'PENDING_REVIEW', 'APPROVED', 'REJECTED')
    ),
    CONSTRAINT ck_academy_profile_revisions_workflow CHECK (
        (status = 'DRAFT'
            AND submitted_at IS NULL
            AND reviewed_at IS NULL
            AND reviewed_by IS NULL
            AND rejection_reason IS NULL)
        OR (status = 'PENDING_REVIEW'
            AND submitted_at IS NOT NULL
            AND reviewed_at IS NULL
            AND reviewed_by IS NULL
            AND rejection_reason IS NULL)
        OR (status = 'APPROVED'
            AND submitted_at IS NOT NULL
            AND reviewed_at IS NOT NULL
            AND reviewed_by IS NOT NULL
            AND rejection_reason IS NULL)
        OR (status = 'REJECTED'
            AND submitted_at IS NOT NULL
            AND reviewed_at IS NOT NULL
            AND reviewed_by IS NOT NULL
            AND btrim(rejection_reason) <> '')
    ),
    CONSTRAINT ck_academy_profile_revisions_review_after_submission CHECK (
        reviewed_at IS NULL OR reviewed_at >= submitted_at
    ),
    CONSTRAINT ck_academy_profile_revisions_updated_after_created CHECK (
        updated_at >= created_at
    )
);

ALTER TABLE academy_profiles
    ADD CONSTRAINT fk_academy_profiles_current_revision
        FOREIGN KEY (current_revision_id)
        REFERENCES academy_profile_revisions (id),
    ADD CONSTRAINT fk_academy_profiles_published_revision
        FOREIGN KEY (published_revision_id)
        REFERENCES academy_profile_revisions (id);

CREATE TABLE academy_revision_modalities (
    revision_id UUID NOT NULL,
    modality_id SMALLINT NOT NULL,

    CONSTRAINT pk_academy_revision_modalities
        PRIMARY KEY (revision_id, modality_id),
    CONSTRAINT fk_academy_revision_modalities_revision
        FOREIGN KEY (revision_id)
        REFERENCES academy_profile_revisions (id) ON DELETE CASCADE,
    CONSTRAINT fk_academy_revision_modalities_modality
        FOREIGN KEY (modality_id)
        REFERENCES modalities (id)
);

CREATE TABLE academy_members (
    academy_id UUID NOT NULL,
    user_id UUID NOT NULL,
    member_role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL,
    deactivated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_academy_members PRIMARY KEY (academy_id, user_id),
    CONSTRAINT fk_academy_members_academy FOREIGN KEY (academy_id)
        REFERENCES academy_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_academy_members_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_academy_members_role CHECK (
        member_role IN ('OWNER', 'ADMIN')
    ),
    CONSTRAINT ck_academy_members_status CHECK (
        status IN ('ACTIVE', 'INACTIVE')
    ),
    CONSTRAINT ck_academy_members_lifecycle CHECK (
        (status = 'ACTIVE' AND deactivated_at IS NULL)
        OR (status = 'INACTIVE'
            AND deactivated_at IS NOT NULL
            AND deactivated_at >= joined_at)
    ),
    CONSTRAINT ck_academy_members_updated_after_created CHECK (
        updated_at >= created_at
    )
);

CREATE TABLE academy_personal_partnerships (
    id UUID PRIMARY KEY,
    academy_id UUID NOT NULL,
    personal_id UUID NOT NULL,
    initiated_by VARCHAR(20) NOT NULL,
    requested_by UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    responded_by UUID,
    requested_at TIMESTAMPTZ NOT NULL,
    responded_at TIMESTAMPTZ,
    ended_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_academy_personal_partnerships_academy
        FOREIGN KEY (academy_id)
        REFERENCES academy_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_academy_personal_partnerships_personal
        FOREIGN KEY (personal_id)
        REFERENCES personal_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_academy_personal_partnerships_requested_by
        FOREIGN KEY (requested_by)
        REFERENCES users (id),
    CONSTRAINT fk_academy_personal_partnerships_responded_by
        FOREIGN KEY (responded_by)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_academy_personal_partnerships_initiator CHECK (
        initiated_by IN ('ACADEMY', 'PERSONAL')
    ),
    CONSTRAINT ck_academy_personal_partnerships_status CHECK (
        status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'ENDED')
    ),
    CONSTRAINT ck_academy_personal_partnerships_lifecycle CHECK (
        (status = 'PENDING'
            AND responded_by IS NULL
            AND responded_at IS NULL
            AND ended_at IS NULL)
        OR (status = 'ACCEPTED'
            AND responded_by IS NOT NULL
            AND responded_at IS NOT NULL
            AND ended_at IS NULL)
        OR (status = 'REJECTED'
            AND responded_by IS NOT NULL
            AND responded_at IS NOT NULL
            AND ended_at IS NULL)
        OR (status = 'ENDED'
            AND responded_at IS NOT NULL
            AND ended_at IS NOT NULL
            AND ended_at >= responded_at)
    ),
    CONSTRAINT ck_academy_personal_partnerships_response_after_request CHECK (
        responded_at IS NULL OR responded_at >= requested_at
    ),
    CONSTRAINT ck_academy_personal_partnerships_updated_after_created CHECK (
        updated_at >= created_at
    )
);

CREATE UNIQUE INDEX uk_academy_profile_revisions_open_work
    ON academy_profile_revisions (academy_id)
    WHERE status IN ('DRAFT', 'PENDING_REVIEW');

CREATE UNIQUE INDEX uk_academy_members_active_owner
    ON academy_members (academy_id)
    WHERE member_role = 'OWNER' AND status = 'ACTIVE';

CREATE UNIQUE INDEX uk_academy_personal_partnerships_open
    ON academy_personal_partnerships (academy_id, personal_id)
    WHERE status IN ('PENDING', 'ACCEPTED');

CREATE INDEX ix_academy_profiles_status
    ON academy_profiles (status);

CREATE INDEX ix_academy_profile_revisions_review_queue
    ON academy_profile_revisions (submitted_at)
    WHERE status = 'PENDING_REVIEW';

CREATE INDEX ix_academy_profile_revisions_location
    ON academy_profile_revisions (state_code, lower(city));

CREATE INDEX ix_academy_revision_modalities_modality
    ON academy_revision_modalities (modality_id);

CREATE INDEX ix_academy_members_user
    ON academy_members (user_id, status);

CREATE INDEX ix_academy_personal_partnerships_personal
    ON academy_personal_partnerships (personal_id, status);
