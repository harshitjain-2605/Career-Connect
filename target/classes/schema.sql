-- ==============================================================================
-- Campus Placement Management System - SQLite Schema (3NF)
-- ==============================================================================
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS companies (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    name            TEXT    NOT NULL UNIQUE,
    industry        TEXT    NOT NULL,
    website         TEXT,
    contact_person  TEXT    NOT NULL,
    contact_email   TEXT    NOT NULL,
    hr_phone        TEXT    NOT NULL,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS users (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    email           TEXT    NOT NULL UNIQUE,
    password_hash   TEXT    NOT NULL,
    role            TEXT    NOT NULL CHECK (role IN ('ADMIN', 'STUDENT', 'RECRUITER')),
    company_id      INTEGER,
    active          INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1)),
    created_at      TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS students (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         INTEGER NOT NULL UNIQUE,
    roll_no         TEXT    NOT NULL UNIQUE,
    full_name       TEXT    NOT NULL,
    email           TEXT    NOT NULL UNIQUE,
    phone           TEXT    NOT NULL,
    branch          TEXT    NOT NULL CHECK (branch IN ('CSE', 'ECE', 'ME', 'EE', 'CHE')),
    batch_year      INTEGER NOT NULL CHECK (batch_year >= 2020 AND batch_year <= 2035),
    cgpa            REAL    NOT NULL CHECK (cgpa >= 0.0 AND cgpa <= 10.0),
    active_backlogs INTEGER NOT NULL DEFAULT 0 CHECK (active_backlogs >= 0),
    skills          TEXT,
    resume_path     TEXT,
    blocked         INTEGER NOT NULL DEFAULT 0 CHECK (blocked IN (0, 1)),
    created_at      TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS drives (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    company_id      INTEGER NOT NULL,
    job_role        TEXT    NOT NULL,
    package_lpa     REAL    NOT NULL CHECK (package_lpa >= 0.0),
    location        TEXT    NOT NULL,
    job_type        TEXT    NOT NULL CHECK (job_type IN ('FULL_TIME', 'INTERNSHIP', 'INTERN_PPO')),
    drive_date      TEXT    NOT NULL,
    last_date       TEXT    NOT NULL,
    min_cgpa        REAL    NOT NULL DEFAULT 0.0 CHECK (min_cgpa >= 0.0 AND min_cgpa <= 10.0),
    max_backlogs    INTEGER NOT NULL DEFAULT 0 CHECK (max_backlogs >= 0),
    batch_year      INTEGER NOT NULL,
    status          TEXT    NOT NULL CHECK (status IN ('UPCOMING', 'OPEN', 'CLOSED', 'COMPLETED')),
    description     TEXT,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS drive_branches (
    drive_id        INTEGER NOT NULL,
    branch          TEXT    NOT NULL CHECK (branch IN ('CSE', 'ECE', 'ME', 'EE', 'CHE')),
    PRIMARY KEY (drive_id, branch),
    FOREIGN KEY (drive_id) REFERENCES drives(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS applications (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id      INTEGER NOT NULL,
    drive_id        INTEGER NOT NULL,
    status          TEXT    NOT NULL CHECK (status IN ('APPLIED', 'SHORTLISTED', 'TEST', 'INTERVIEW', 'SELECTED', 'REJECTED')),
    applied_at      TEXT    NOT NULL DEFAULT (datetime('now')),
    updated_at      TEXT    NOT NULL DEFAULT (datetime('now')),
    UNIQUE (student_id, drive_id),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (drive_id)   REFERENCES drives(id)   ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS application_status_history (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    application_id      INTEGER NOT NULL,
    from_status         TEXT,
    to_status           TEXT    NOT NULL CHECK (to_status IN ('APPLIED', 'SHORTLISTED', 'TEST', 'INTERVIEW', 'SELECTED', 'REJECTED')),
    changed_by_user_id  INTEGER,
    remarks             TEXT,
    changed_at          TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (application_id)     REFERENCES applications(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by_user_id) REFERENCES users(id)        ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS offers (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    application_id  INTEGER NOT NULL UNIQUE,
    student_id      INTEGER NOT NULL,
    drive_id        INTEGER NOT NULL,
    package_lpa     REAL    NOT NULL CHECK (package_lpa >= 0.0),
    joining_date    TEXT    NOT NULL,
    status          TEXT    NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED')),
    issued_at       TEXT    NOT NULL DEFAULT (datetime('now')),
    responded_at    TEXT,
    FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id)     REFERENCES students(id)     ON DELETE CASCADE,
    FOREIGN KEY (drive_id)       REFERENCES drives(id)       ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notifications (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         INTEGER NOT NULL,
    title           TEXT    NOT NULL,
    message         TEXT    NOT NULL,
    is_read         INTEGER NOT NULL DEFAULT 0 CHECK (is_read IN (0, 1)),
    created_at      TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS audit_log (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    actor_user_id   INTEGER,
    action          TEXT    NOT NULL,
    entity_type     TEXT    NOT NULL,
    entity_id       INTEGER,
    details         TEXT,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Indexes for frequent queries
CREATE INDEX IF NOT EXISTS idx_students_branch       ON students(branch);
CREATE INDEX IF NOT EXISTS idx_students_cgpa         ON students(cgpa);
CREATE INDEX IF NOT EXISTS idx_drives_status         ON drives(status);
CREATE INDEX IF NOT EXISTS idx_drives_company        ON drives(company_id);
CREATE INDEX IF NOT EXISTS idx_applications_student  ON applications(student_id);
CREATE INDEX IF NOT EXISTS idx_applications_drive    ON applications(drive_id);
CREATE INDEX IF NOT EXISTS idx_offers_student        ON offers(student_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user    ON notifications(user_id, is_read);
