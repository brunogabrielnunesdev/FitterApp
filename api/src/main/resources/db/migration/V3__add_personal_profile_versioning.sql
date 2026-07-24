ALTER TABLE roles
    DROP CONSTRAINT ck_roles_name;

ALTER TABLE roles
    ADD CONSTRAINT ck_roles_name CHECK (
        name IN ('STUDENT', 'PERSONAL', 'ADMIN', 'OWNER')
    );

INSERT INTO roles (id, name)
VALUES (4, 'OWNER');

ALTER TABLE personal_profiles
    DROP CONSTRAINT ck_personal_profiles_status,
    DROP CONSTRAINT ck_personal_profiles_publication;

ALTER TABLE personal_profiles
    ALTER COLUMN full_name DROP NOT NULL;

ALTER TABLE personal_profiles
    ADD CONSTRAINT ck_personal_profiles_status CHECK (
        status IN (
            'DRAFT',
            'PENDING_REVIEW',
            'APPROVED',
            'PUBLISHED',
            'REJECTED',
            'SUSPENDED'
        )
    ),
    ADD CONSTRAINT ck_personal_profiles_publication CHECK (
        (status = 'PUBLISHED' AND published_at IS NOT NULL)
        OR status = 'SUSPENDED'
        OR (status IN ('DRAFT', 'PENDING_REVIEW', 'APPROVED', 'REJECTED')
            AND published_at IS NULL)
    );

CREATE TABLE personal_crefs (
    id UUID PRIMARY KEY,
    personal_id UUID NOT NULL,
    registration_code VARCHAR(40) NOT NULL,
    document_image_key VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    verified_at TIMESTAMPTZ,
    verified_by UUID,
    rejection_reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_personal_crefs_registration_code UNIQUE (registration_code),
    CONSTRAINT fk_personal_crefs_personal FOREIGN KEY (personal_id)
        REFERENCES personal_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_personal_crefs_verified_by FOREIGN KEY (verified_by)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_personal_crefs_registration_code CHECK (
        registration_code = upper(btrim(registration_code))
        AND btrim(registration_code) <> ''
    ),
    CONSTRAINT ck_personal_crefs_document_key CHECK (
        document_image_key ~
            '^private/crefs/[0-9a-f-]+/[0-9a-f-]+\.webp$'
        AND document_image_key NOT LIKE '%..%'
    ),
    CONSTRAINT ck_personal_crefs_status CHECK (
        status IN ('PENDING_REVIEW', 'VERIFIED', 'REJECTED')
    ),
    CONSTRAINT ck_personal_crefs_verification CHECK (
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
    CONSTRAINT ck_personal_crefs_updated_after_created CHECK (
        updated_at >= created_at
    )
);

CREATE TABLE personal_profile_revisions (
    id UUID PRIMARY KEY,
    personal_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    full_name VARCHAR(120),
    biography VARCHAR(1500),
    whatsapp VARCHAR(20),
    profile_image_key VARCHAR(255),
    experience_started_year SMALLINT,
    certifications VARCHAR(1000),
    gyms_description VARCHAR(500),
    starting_price_cents INTEGER,
    price_unit VARCHAR(30),
    cref_id UUID,
    status VARCHAR(30) NOT NULL,
    requires_review BOOLEAN NOT NULL,
    rejection_reason VARCHAR(500),
    created_by UUID,
    submitted_at TIMESTAMPTZ,
    reviewed_at TIMESTAMPTZ,
    reviewed_by UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_personal_profile_revisions_version
        UNIQUE (personal_id, version_number),
    CONSTRAINT fk_personal_profile_revisions_personal FOREIGN KEY (personal_id)
        REFERENCES personal_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_personal_profile_revisions_cref FOREIGN KEY (cref_id)
        REFERENCES personal_crefs (id),
    CONSTRAINT fk_personal_profile_revisions_created_by FOREIGN KEY (created_by)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_personal_profile_revisions_reviewed_by FOREIGN KEY (reviewed_by)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_personal_profile_revisions_version_positive CHECK (
        version_number > 0
    ),
    CONSTRAINT ck_personal_profile_revisions_full_name CHECK (
        full_name IS NULL OR btrim(full_name) <> ''
    ),
    CONSTRAINT ck_personal_profile_revisions_biography CHECK (
        biography IS NULL OR btrim(biography) <> ''
    ),
    CONSTRAINT ck_personal_profile_revisions_whatsapp CHECK (
        whatsapp IS NULL OR whatsapp ~ '^\+[1-9][0-9]{7,14}$'
    ),
    CONSTRAINT ck_personal_profile_revisions_image_key CHECK (
        profile_image_key IS NULL
        OR (
            profile_image_key ~ '^personals/[0-9a-f-]+/[0-9a-f-]+\.webp$'
            AND profile_image_key NOT LIKE '%..%'
        )
    ),
    CONSTRAINT ck_personal_profile_revisions_experience_year CHECK (
        experience_started_year IS NULL
        OR experience_started_year BETWEEN 1900 AND 2100
    ),
    CONSTRAINT ck_personal_profile_revisions_certifications CHECK (
        certifications IS NULL OR btrim(certifications) <> ''
    ),
    CONSTRAINT ck_personal_profile_revisions_gyms CHECK (
        gyms_description IS NULL OR btrim(gyms_description) <> ''
    ),
    CONSTRAINT ck_personal_profile_revisions_price_positive CHECK (
        starting_price_cents IS NULL OR starting_price_cents > 0
    ),
    CONSTRAINT ck_personal_profile_revisions_price_pair CHECK (
        (starting_price_cents IS NULL AND price_unit IS NULL)
        OR (starting_price_cents IS NOT NULL AND price_unit IS NOT NULL)
    ),
    CONSTRAINT ck_personal_profile_revisions_price_unit CHECK (
        price_unit IS NULL
        OR price_unit IN ('PER_SESSION', 'MONTHLY', 'CONSULTATION')
    ),
    CONSTRAINT ck_personal_profile_revisions_status CHECK (
        status IN ('DRAFT', 'PENDING_REVIEW', 'APPROVED', 'REJECTED')
    ),
    CONSTRAINT ck_personal_profile_revisions_workflow CHECK (
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
            AND rejection_reason IS NULL)
        OR (status = 'REJECTED'
            AND submitted_at IS NOT NULL
            AND reviewed_at IS NOT NULL
            AND btrim(rejection_reason) <> '')
    ),
    CONSTRAINT ck_personal_profile_revisions_review_after_submission CHECK (
        reviewed_at IS NULL OR reviewed_at >= submitted_at
    ),
    CONSTRAINT ck_personal_profile_revisions_updated_after_created CHECK (
        updated_at >= created_at
    )
);

ALTER TABLE personal_profiles
    ADD COLUMN current_revision_id UUID,
    ADD COLUMN published_revision_id UUID;

ALTER TABLE personal_profiles
    ADD CONSTRAINT fk_personal_profiles_current_revision
        FOREIGN KEY (current_revision_id)
        REFERENCES personal_profile_revisions (id),
    ADD CONSTRAINT fk_personal_profiles_published_revision
        FOREIGN KEY (published_revision_id)
        REFERENCES personal_profile_revisions (id);

CREATE TABLE personal_revision_modalities (
    revision_id UUID NOT NULL,
    modality_id SMALLINT NOT NULL,

    CONSTRAINT pk_personal_revision_modalities
        PRIMARY KEY (revision_id, modality_id),
    CONSTRAINT fk_personal_revision_modalities_revision FOREIGN KEY (revision_id)
        REFERENCES personal_profile_revisions (id) ON DELETE CASCADE,
    CONSTRAINT fk_personal_revision_modalities_modality FOREIGN KEY (modality_id)
        REFERENCES modalities (id)
);

CREATE TABLE personal_revision_service_modes (
    revision_id UUID NOT NULL,
    service_mode VARCHAR(30) NOT NULL,

    CONSTRAINT pk_personal_revision_service_modes
        PRIMARY KEY (revision_id, service_mode),
    CONSTRAINT fk_personal_revision_service_modes_revision FOREIGN KEY (revision_id)
        REFERENCES personal_profile_revisions (id) ON DELETE CASCADE,
    CONSTRAINT ck_personal_revision_service_modes_value CHECK (
        service_mode IN ('IN_PERSON', 'HOME_VISIT', 'ONLINE')
    )
);

CREATE TABLE personal_revision_service_areas (
    id UUID PRIMARY KEY,
    revision_id UUID NOT NULL,
    city VARCHAR(100) NOT NULL,
    state_code CHAR(2) NOT NULL,
    neighborhood VARCHAR(100),
    description VARCHAR(200),
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_personal_revision_service_areas_revision FOREIGN KEY (revision_id)
        REFERENCES personal_profile_revisions (id) ON DELETE CASCADE,
    CONSTRAINT uk_personal_revision_service_areas_location
        UNIQUE NULLS NOT DISTINCT (revision_id, state_code, city, neighborhood),
    CONSTRAINT ck_personal_revision_service_areas_city CHECK (
        btrim(city) <> ''
    ),
    CONSTRAINT ck_personal_revision_service_areas_state_code CHECK (
        state_code ~ '^[A-Z]{2}$'
    ),
    CONSTRAINT ck_personal_revision_service_areas_neighborhood CHECK (
        neighborhood IS NULL OR btrim(neighborhood) <> ''
    ),
    CONSTRAINT ck_personal_revision_service_areas_description CHECK (
        description IS NULL OR btrim(description) <> ''
    )
);

CREATE UNIQUE INDEX uk_personal_profile_revisions_open_work
    ON personal_profile_revisions (personal_id)
    WHERE status IN ('DRAFT', 'PENDING_REVIEW');

CREATE INDEX ix_personal_crefs_personal_id
    ON personal_crefs (personal_id);

CREATE INDEX ix_personal_crefs_status
    ON personal_crefs (status);

CREATE INDEX ix_personal_profile_revisions_personal_status
    ON personal_profile_revisions (personal_id, status);

CREATE INDEX ix_personal_profile_revisions_review_queue
    ON personal_profile_revisions (submitted_at)
    WHERE status = 'PENDING_REVIEW';

CREATE INDEX ix_personal_revision_modalities_modality_id
    ON personal_revision_modalities (modality_id);

CREATE INDEX ix_personal_revision_service_areas_location
    ON personal_revision_service_areas (
        state_code,
        lower(city),
        lower(neighborhood)
    );

INSERT INTO personal_profile_revisions (
    id,
    personal_id,
    version_number,
    full_name,
    biography,
    whatsapp,
    profile_image_key,
    experience_started_year,
    certifications,
    gyms_description,
    starting_price_cents,
    price_unit,
    status,
    requires_review,
    created_by,
    submitted_at,
    reviewed_at,
    created_at,
    updated_at
)
SELECT
    md5(id::text || ':revision:1')::UUID,
    id,
    1,
    full_name,
    biography,
    whatsapp,
    profile_image_key,
    experience_started_year,
    certifications,
    gyms_description,
    starting_price_cents,
    price_unit,
    CASE
        WHEN status = 'DRAFT' THEN 'DRAFT'
        ELSE 'APPROVED'
    END,
    FALSE,
    user_id,
    CASE
        WHEN status = 'DRAFT' THEN NULL
        ELSE created_at
    END,
    CASE
        WHEN status = 'DRAFT' THEN NULL
        ELSE updated_at
    END,
    created_at,
    updated_at
FROM personal_profiles;

INSERT INTO personal_revision_modalities (revision_id, modality_id)
SELECT
    md5(personal_id::text || ':revision:1')::UUID,
    modality_id
FROM personal_modalities;

INSERT INTO personal_revision_service_modes (revision_id, service_mode)
SELECT
    md5(personal_id::text || ':revision:1')::UUID,
    service_mode
FROM personal_service_modes;

INSERT INTO personal_revision_service_areas (
    id,
    revision_id,
    city,
    state_code,
    neighborhood,
    description,
    created_at
)
SELECT
    id,
    md5(personal_id::text || ':revision:1')::UUID,
    city,
    state_code,
    neighborhood,
    description,
    created_at
FROM personal_service_areas;

UPDATE personal_profiles
SET
    current_revision_id = md5(id::text || ':revision:1')::UUID,
    published_revision_id = CASE
        WHEN status IN ('PUBLISHED', 'SUSPENDED')
            THEN md5(id::text || ':revision:1')::UUID
        ELSE NULL
    END;
