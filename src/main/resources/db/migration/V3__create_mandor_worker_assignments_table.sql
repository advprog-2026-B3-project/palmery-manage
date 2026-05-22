CREATE TABLE mandor_worker_assignments (
    worker_id    UUID         NOT NULL PRIMARY KEY,
    mandor_id    UUID         NOT NULL,
    assigned_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_mandor_worker_mandor ON mandor_worker_assignments (mandor_id);
