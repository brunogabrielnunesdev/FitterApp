CREATE TABLE personal_profiles (
    id UUID PRIMARY KEY,
    user_id UUID,
    full_name VARCHAR(120) NOT NULL,
    slug VARCHAR(150) NOT NULL,
    biography VARCHAR(1500),
    whatsapp VARCHAR(20),
    profile_image_key VARCHAR(255),
    experience_started_year SMALLINT,
    certifications VARCHAR(1000),
    gyms_description VARCHAR(500),
    starting_price_cents INTEGER,
    price_unit VARCHAR(30),
    status VARCHAR(30) NOT NULL,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_personal_profiles_user_id UNIQUE (user_id),
    CONSTRAINT uk_personal_profiles_slug UNIQUE (slug),
    CONSTRAINT fk_personal_profiles_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_personal_profiles_full_name_not_blank CHECK (btrim(full_name) <> ''),
    CONSTRAINT ck_personal_profiles_slug CHECK (
        slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'
    ),
    CONSTRAINT ck_personal_profiles_biography_not_blank CHECK (
        biography IS NULL OR btrim(biography) <> ''
    ),
    CONSTRAINT ck_personal_profiles_whatsapp_e164 CHECK (
        whatsapp IS NULL OR whatsapp ~ '^\+[1-9][0-9]{7,14}$'
    ),
    CONSTRAINT ck_personal_profiles_image_key CHECK (
        profile_image_key IS NULL
        OR (
            profile_image_key ~ '^personals/[0-9a-f-]+/[0-9a-f-]+\.webp$'
            AND profile_image_key NOT LIKE '%..%'
        )
    ),
    CONSTRAINT ck_personal_profiles_experience_year CHECK (
        experience_started_year IS NULL
        OR experience_started_year BETWEEN 1900 AND 2100
    ),
    CONSTRAINT ck_personal_profiles_certifications_not_blank CHECK (
        certifications IS NULL OR btrim(certifications) <> ''
    ),
    CONSTRAINT ck_personal_profiles_gyms_not_blank CHECK (
        gyms_description IS NULL OR btrim(gyms_description) <> ''
    ),
    CONSTRAINT ck_personal_profiles_price_positive CHECK (
        starting_price_cents IS NULL OR starting_price_cents > 0
    ),
    CONSTRAINT ck_personal_profiles_price_pair CHECK (
        (starting_price_cents IS NULL AND price_unit IS NULL)
        OR (starting_price_cents IS NOT NULL AND price_unit IS NOT NULL)
    ),
    CONSTRAINT ck_personal_profiles_price_unit CHECK (
        price_unit IS NULL
        OR price_unit IN ('PER_SESSION', 'MONTHLY', 'CONSULTATION')
    ),
    CONSTRAINT ck_personal_profiles_status CHECK (
        status IN ('DRAFT', 'PUBLISHED', 'SUSPENDED')
    ),
    CONSTRAINT ck_personal_profiles_publication CHECK (
        (status = 'DRAFT' AND published_at IS NULL)
        OR (status = 'PUBLISHED' AND published_at IS NOT NULL)
        OR status = 'SUSPENDED'
    ),
    CONSTRAINT ck_personal_profiles_updated_after_created CHECK (
        updated_at >= created_at
    )
);

CREATE TABLE modalities (
    id SMALLINT PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    slug VARCHAR(80) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_modalities_name UNIQUE (name),
    CONSTRAINT uk_modalities_slug UNIQUE (slug),
    CONSTRAINT ck_modalities_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT ck_modalities_slug CHECK (
        slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'
    ),
    CONSTRAINT ck_modalities_updated_after_created CHECK (
        updated_at >= created_at
    )
);

CREATE TABLE personal_modalities (
    personal_id UUID NOT NULL,
    modality_id SMALLINT NOT NULL,

    CONSTRAINT pk_personal_modalities PRIMARY KEY (personal_id, modality_id),
    CONSTRAINT fk_personal_modalities_personal FOREIGN KEY (personal_id)
        REFERENCES personal_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_personal_modalities_modality FOREIGN KEY (modality_id)
        REFERENCES modalities (id)
);

CREATE TABLE personal_service_modes (
    personal_id UUID NOT NULL,
    service_mode VARCHAR(30) NOT NULL,

    CONSTRAINT pk_personal_service_modes PRIMARY KEY (personal_id, service_mode),
    CONSTRAINT fk_personal_service_modes_personal FOREIGN KEY (personal_id)
        REFERENCES personal_profiles (id) ON DELETE CASCADE,
    CONSTRAINT ck_personal_service_modes_value CHECK (
        service_mode IN ('IN_PERSON', 'HOME_VISIT', 'ONLINE')
    )
);

CREATE TABLE personal_service_areas (
    id UUID PRIMARY KEY,
    personal_id UUID NOT NULL,
    city VARCHAR(100) NOT NULL,
    state_code CHAR(2) NOT NULL,
    neighborhood VARCHAR(100),
    description VARCHAR(200),
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_personal_service_areas_personal FOREIGN KEY (personal_id)
        REFERENCES personal_profiles (id) ON DELETE CASCADE,
    CONSTRAINT uk_personal_service_areas_location
        UNIQUE NULLS NOT DISTINCT (personal_id, state_code, city, neighborhood),
    CONSTRAINT ck_personal_service_areas_city_not_blank CHECK (btrim(city) <> ''),
    CONSTRAINT ck_personal_service_areas_state_code CHECK (
        state_code ~ '^[A-Z]{2}$'
    ),
    CONSTRAINT ck_personal_service_areas_neighborhood_not_blank CHECK (
        neighborhood IS NULL OR btrim(neighborhood) <> ''
    ),
    CONSTRAINT ck_personal_service_areas_description_not_blank CHECK (
        description IS NULL OR btrim(description) <> ''
    )
);

CREATE INDEX ix_personal_profiles_status
    ON personal_profiles (status);

CREATE INDEX ix_personal_profiles_name_search
    ON personal_profiles (lower(full_name));

CREATE INDEX ix_personal_modalities_modality_id
    ON personal_modalities (modality_id);

CREATE INDEX ix_personal_service_areas_location
    ON personal_service_areas (state_code, lower(city), lower(neighborhood));

INSERT INTO modalities (id, name, slug, active, created_at, updated_at)
VALUES
    (1, 'Musculação', 'musculacao', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Emagrecimento', 'emagrecimento', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'Hipertrofia', 'hipertrofia', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 'Treinamento funcional', 'treinamento-funcional', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 'Corrida', 'corrida', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 'Mobilidade', 'mobilidade', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7, 'Terceira idade', 'terceira-idade', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, 'Preparação física', 'preparacao-fisica', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
