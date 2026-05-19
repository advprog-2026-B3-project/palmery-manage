CREATE TABLE plantation_assignments (
    id              UUID            NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    plantation_id   UUID            NOT NULL REFERENCES plantation(id) ON DELETE CASCADE,
    personnel_id    UUID            NOT NULL,
    role            VARCHAR(20)     NOT NULL,
    assigned_at     TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_plantation_personnel_role UNIQUE (plantation_id, personnel_id, role)
);

CREATE INDEX idx_assignment_plantation ON plantation_assignments (plantation_id);
CREATE INDEX idx_assignment_personnel ON plantation_assignments (personnel_id);
CREATE INDEX idx_assignment_role ON plantation_assignments (role);
