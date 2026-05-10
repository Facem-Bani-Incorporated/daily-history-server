CREATE TABLE roles (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(20)
);

CREATE UNIQUE INDEX roles_name_idx ON roles (name);

CREATE TABLE users (
    id               BIGSERIAL    PRIMARY KEY,
    username         VARCHAR(50)  NOT NULL,
    email            VARCHAR(50),
    password         VARCHAR(120),
    avatar_url       VARCHAR(512),
    auth_provider    VARCHAR(31)  NOT NULL,
    provider_user_id VARCHAR(128),
    is_pro           BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uc_users_username UNIQUE (username),
    CONSTRAINT uc_users_email    UNIQUE (email),
    CONSTRAINT uc_users_provider UNIQUE (auth_provider, provider_user_id)
);

CREATE TABLE user_roles (
    user_id BIGINT  NOT NULL REFERENCES users(id),
    role_id INTEGER NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE translations (
    id BIGSERIAL PRIMARY KEY,
    en TEXT NOT NULL,
    ro TEXT NOT NULL,
    es TEXT NOT NULL,
    de TEXT NOT NULL,
    fr TEXT NOT NULL
);

CREATE TABLE daily_content (
    id             BIGSERIAL PRIMARY KEY,
    date_processed DATE
);

CREATE UNIQUE INDEX daily_content_date_idx ON daily_content (date_processed);

CREATE TABLE events (
    id                        BIGSERIAL        PRIMARY KEY,
    category                  VARCHAR(31)      NOT NULL,
    title_translations_id     BIGINT           NOT NULL UNIQUE REFERENCES translations(id),
    narrative_translations_id BIGINT           NOT NULL UNIQUE REFERENCES translations(id),
    event_date                DATE             NOT NULL,
    is_pro                    BOOLEAN          NOT NULL DEFAULT FALSE,
    location                  VARCHAR(255),
    impact_score              DOUBLE PRECISION NOT NULL,
    source_url                VARCHAR(255)     NOT NULL,
    page_views_30d            INTEGER,
    daily_content_id          BIGINT           NOT NULL REFERENCES daily_content(id)
);

CREATE TABLE event_gallery (
    event_id  BIGINT       NOT NULL REFERENCES events(id),
    image_url VARCHAR(255) NOT NULL
);

CREATE TABLE user_gamification (
    id                    BIGSERIAL PRIMARY KEY,
    user_id               BIGINT    NOT NULL UNIQUE REFERENCES users(id),
    total_xp              INTEGER   NOT NULL DEFAULT 0,
    current_streak        INTEGER   NOT NULL DEFAULT 0,
    longest_streak        INTEGER   NOT NULL DEFAULT 0,
    total_events_read     INTEGER   NOT NULL DEFAULT 0,
    daily_goals_completed INTEGER   NOT NULL DEFAULT 0,
    last_active_date      DATE,
    gamification_data     TEXT
);

CREATE TABLE user_saved_events (
    user_gamification_id BIGINT NOT NULL REFERENCES user_gamification(id),
    event_id             BIGINT NOT NULL
);

CREATE TABLE support_messages (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id),
    category   VARCHAR(31)  NOT NULL,
    subject    VARCHAR(255) NOT NULL,
    message    TEXT         NOT NULL,
    created_at TIMESTAMP    NOT NULL
);
