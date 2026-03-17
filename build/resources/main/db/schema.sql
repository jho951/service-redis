-- ─────────────────────────────
-- 1) 테이블 / 인덱스
-- ─────────────────────────────
CREATE TABLE IF NOT EXISTS drawer (
                                      id         UUID PRIMARY KEY,
                                      title      VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ NULL,
    is_active  BOOLEAN GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN TRUE ELSE FALSE END) STORED,
    version    INTEGER NOT NULL DEFAULT 0
    ) @@

    CREATE TABLE IF NOT EXISTS drawer_payload (
                                                  drawer_id   UUID PRIMARY KEY REFERENCES drawer(id) ON DELETE CASCADE,
    vector_json JSONB NOT NULL
    ) @@

    CREATE UNIQUE INDEX IF NOT EXISTS uk_drawer_title_active ON drawer (title, is_active) @@
    CREATE INDEX IF NOT EXISTS idx_drawer_deleted_at     ON drawer (deleted_at) @@
    CREATE INDEX IF NOT EXISTS idx_drawer_updated_at_id  ON drawer (updated_at, id) @@

-- USERS / ROLES -------------------------------------------------------
    CREATE TABLE IF NOT EXISTS users (
                                         id         UUID PRIMARY KEY,
                                         username   TEXT UNIQUE NOT NULL,
                                         password   TEXT NOT NULL,
                                         enabled    BOOLEAN NOT NULL DEFAULT TRUE,
                                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ NULL,
    is_active  BOOLEAN GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN TRUE ELSE FALSE END) STORED
    ) @@

    CREATE TABLE IF NOT EXISTS roles (
                                         id   SERIAL PRIMARY KEY,
                                         name TEXT UNIQUE NOT NULL
                                     ) @@

    CREATE TABLE IF NOT EXISTS user_roles (
                                              user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INT  NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
    ) @@

-- 최소 역할 시드
    INSERT INTO roles(name) VALUES ('ROLE_USER')  ON CONFLICT (name) DO NOTHING @@
    INSERT INTO roles(name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING @@

    -- ─────────────────────────────
-- 2) updated_at 자동 갱신 함수 (PostgreSQL)
-- ─────────────────────────────
    CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS trigger
    LANGUAGE plpgsql
    AS $func$
BEGIN
  NEW.updated_at := NOW();
RETURN NEW;
END;
$func$ @@

-- ─────────────────────────────
-- 3) 트리거 (재실행 안전)
-- ─────────────────────────────
DROP TRIGGER IF EXISTS users_set_updated_at ON users @@
CREATE TRIGGER users_set_updated_at
BEFORE UPDATE ON users
                  FOR EACH ROW
                  EXECUTE FUNCTION set_updated_at() @@

                  DROP TRIGGER IF EXISTS drawer_set_updated_at ON drawer @@
                  CREATE TRIGGER drawer_set_updated_at
                  BEFORE UPDATE ON drawer
                             FOR EACH ROW
                             EXECUTE FUNCTION set_updated_at() @@

-- drawer_payload에도 updated_at을 쓰고 싶다면:
-- ALTER TABLE drawer_payload ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW() @@
-- DROP TRIGGER IF EXISTS drawer_payload_set_updated_at ON drawer_payload @@
-- CREATE TRIGGER drawer_payload_set_updated_at
-- BEFORE UPDATE ON drawer_payload
-- FOR EACH ROW
-- EXECUTE FUNCTION set_updated_at() @@
