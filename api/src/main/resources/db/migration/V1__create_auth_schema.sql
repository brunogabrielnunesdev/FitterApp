CREATE TABLE users (
    id UUID PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    email_verified_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_full_name_not_blank CHECK (btrim(full_name) <> ''),
    CONSTRAINT ck_users_email_normalized CHECK (email = lower(btrim(email))),
    CONSTRAINT ck_users_phone_e164 CHECK (phone_number ~ '^\+[1-9][0-9]{7,14}$'),
    CONSTRAINT ck_users_status CHECK (
        status IN ('PENDING_VERIFICATION', 'ACTIVE', 'BLOCKED')
    ),
    CONSTRAINT ck_users_updated_after_created CHECK (updated_at >= created_at),
    CONSTRAINT ck_users_verification_status CHECK (
        (status = 'PENDING_VERIFICATION' AND email_verified_at IS NULL)
        OR status IN ('ACTIVE', 'BLOCKED')
    )
);

CREATE TABLE roles (
    id SMALLINT PRIMARY KEY,
    name VARCHAR(30) NOT NULL,

    CONSTRAINT uk_roles_name UNIQUE (name),
    CONSTRAINT ck_roles_name CHECK (name IN ('STUDENT', 'PERSONAL', 'ADMIN'))
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id SMALLINT NOT NULL,
    granted_at TIMESTAMPTZ NOT NULL,
    granted_by UUID,

    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id)
        REFERENCES roles (id),
    CONSTRAINT fk_user_roles_granted_by FOREIGN KEY (granted_by)
        REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    family_id UUID NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    last_used_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    replaced_by_id UUID,
    user_agent VARCHAR(500),
    ip_address INET,

    CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_refresh_tokens_replacement FOREIGN KEY (replaced_by_id)
        REFERENCES refresh_tokens (id),
    CONSTRAINT ck_refresh_tokens_hash CHECK (token_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_refresh_tokens_expiration CHECK (expires_at > created_at),
    CONSTRAINT ck_refresh_tokens_last_used CHECK (
        last_used_at IS NULL OR last_used_at >= created_at
    ),
    CONSTRAINT ck_refresh_tokens_revoked CHECK (
        revoked_at IS NULL OR revoked_at >= created_at
    ),
    CONSTRAINT ck_refresh_tokens_not_self_replaced CHECK (
        replaced_by_id IS NULL OR replaced_by_id <> id
    )
);

CREATE TABLE email_verification_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_email_verification_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_email_verification_tokens_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_email_verification_tokens_hash CHECK (token_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_email_verification_tokens_expiration CHECK (expires_at > created_at),
    CONSTRAINT ck_email_verification_tokens_used CHECK (
        used_at IS NULL OR used_at >= created_at
    )
);

CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_password_reset_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_password_reset_tokens_hash CHECK (token_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_password_reset_tokens_expiration CHECK (expires_at > created_at),
    CONSTRAINT ck_password_reset_tokens_used CHECK (
        used_at IS NULL OR used_at >= created_at
    )
);

CREATE INDEX ix_user_roles_role_id
    ON user_roles (role_id);

CREATE INDEX ix_refresh_tokens_user_revoked
    ON refresh_tokens (user_id, revoked_at);

CREATE INDEX ix_refresh_tokens_family_id
    ON refresh_tokens (family_id);

CREATE INDEX ix_refresh_tokens_expires_at
    ON refresh_tokens (expires_at);

CREATE INDEX ix_email_verification_tokens_user_id
    ON email_verification_tokens (user_id);

CREATE INDEX ix_email_verification_tokens_expires_at
    ON email_verification_tokens (expires_at);

CREATE INDEX ix_password_reset_tokens_user_id
    ON password_reset_tokens (user_id);

CREATE INDEX ix_password_reset_tokens_expires_at
    ON password_reset_tokens (expires_at);

INSERT INTO roles (id, name)
VALUES
    (1, 'STUDENT'),
    (2, 'PERSONAL'),
    (3, 'ADMIN');
