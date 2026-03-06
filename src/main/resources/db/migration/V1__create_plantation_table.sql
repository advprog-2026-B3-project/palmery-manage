CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE plantation (
    id          UUID            NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name        VARCHAR(255)    NOT NULL,
    code        VARCHAR(100)    NOT NULL UNIQUE,
    area_ha     DOUBLE PRECISION NOT NULL,

    -- Top-Left coordinate
    coord_tl_lat DOUBLE PRECISION NOT NULL,
    coord_tl_lon DOUBLE PRECISION NOT NULL,

    -- Top-Right coordinate
    coord_tr_lat DOUBLE PRECISION NOT NULL,
    coord_tr_lon DOUBLE PRECISION NOT NULL,

    -- Bottom-Right coordinate
    coord_br_lat DOUBLE PRECISION NOT NULL,
    coord_br_lon DOUBLE PRECISION NOT NULL,

    -- Bottom-Left coordinate
    coord_bl_lat DOUBLE PRECISION NOT NULL,
    coord_bl_lon DOUBLE PRECISION NOT NULL,

    is_active   BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_plantation_name ON plantation (name);
CREATE INDEX idx_plantation_code ON plantation (code);
CREATE INDEX idx_plantation_is_active ON plantation (is_active);
