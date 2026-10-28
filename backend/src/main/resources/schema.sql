CREATE TABLE IF NOT EXISTS users (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username      VARCHAR(30)  NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS posts (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    image_name VARCHAR(100)  NOT NULL,
    caption    VARCHAR(2200) NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS posts_created_at_idx ON posts (created_at DESC);

CREATE TABLE IF NOT EXISTS likes (
    user_id    BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    post_id    BIGINT      NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, post_id)
);

CREATE INDEX IF NOT EXISTS likes_post_id_idx ON likes (post_id);

CREATE TABLE IF NOT EXISTS comments (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    post_id    BIGINT        NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    user_id    BIGINT        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    text       VARCHAR(2200) NOT NULL,
    created_at TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS comments_post_id_idx ON comments (post_id, created_at);
