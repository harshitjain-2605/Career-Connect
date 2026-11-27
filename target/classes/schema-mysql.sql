-- ==============================================================================
-- Campus Placement Management System - MySQL 8+ Schema (3NF)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS companies (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150) NOT NULL UNIQUE,
    industry        VARCHAR(100) NOT NULL,
    website         VARCHAR(255),
    contact_person  VARCHAR(120) NOT NULL,
    contact_email   VARCHAR(150) NOT NULL,
    hr_phone        VARCHAR(25)  NOT NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS users (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            ENUM('ADMIN', 'STUDENT', 'RECRUITER') NOT NULL,
    company_id      INT NULL,
    active          TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS students (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT          NOT NULL UNIQUE,
    roll_no         VARCHAR(30)  NOT NULL UNIQUE,
    full_name       VARCHAR(150) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    phone           VARCHAR(25)  NOT NULL,
    branch          ENUM('CSE', 'ECE', 'ME', 'EE', 'CHE') NOT NULL,
    batch_year      INT          NOT NULL CHECK (batch_year >= 2020 AND batch_year <= 2035),
    cgpa            DECIMAL(4,2) NOT NULL CHECK (cgpa >= 0.0 AND cgpa <= 10.0),
    active_backlogs INT          NOT NULL DEFAULT 0 CHECK (active_backlogs >= 0),
    skills          TEXT,
    resume_path     VARCHAR(500),
    blocked         TINYINT(1)   NOT NULL DEFAULT 0,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_students_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS drives (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    company_id      INT          NOT NULL,
    job_role        VARCHAR(150) NOT NULL,
    package_lpa     DECIMAL(8,2) NOT NULL CHECK (package_lpa >= 0.0),
    location        VARCHAR(120) NOT NULL,
    job_type        ENUM('FULL_TIME', 'INTERNSHIP', 'INTERN_PPO') NOT NULL,
    drive_date      DATE         NOT NULL,
    last_date       DATE         NOT NULL,
    min_cgpa        DECIMAL(4,2) NOT NULL DEFAULT 0.0 CHECK (min_cgpa >= 0.0 AND min_cgpa <= 10.0),
    max_backlogs    INT          NOT NULL DEFAULT 0 CHECK (max_backlogs >= 0),
    batch_year      INT          NOT NULL,
    status          ENUM('UPCOMING', 'OPEN', 'CLOSED', 'COMPLETED') NOT NULL,
    description     TEXT,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_drives_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS drive_branches (
    drive_id        INT NOT NULL,
    branch          ENUM('CSE', 'ECE', 'ME', 'EE', 'CHE') NOT NULL,
    PRIMARY KEY (drive_id, branch),
    CONSTRAINT fk_db_drive FOREIGN KEY (drive_id) REFERENCES drives(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS applications (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    student_id      INT NOT NULL,
    drive_id        INT NOT NULL,
    status          ENUM('APPLIED', 'SHORTLISTED', 'TEST', 'INTERVIEW', 'SELECTED', 'REJECTED') NOT NULL,
    applied_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_student_drive (student_id, drive_id),
    CONSTRAINT fk_app_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_drive   FOREIGN KEY (drive_id)   REFERENCES drives(id)   ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS application_status_history (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    application_id      INT NOT NULL,
    from_status         VARCHAR(30),
    to_status           ENUM('APPLIED', 'SHORTLISTED', 'TEST', 'INTERVIEW', 'SELECTED', 'REJECTED') NOT NULL,
    changed_by_user_id  INT NULL,
    remarks             TEXT,
    changed_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hist_app  FOREIGN KEY (application_id)     REFERENCES applications(id) ON DELETE CASCADE,
    CONSTRAINT fk_hist_user FOREIGN KEY (changed_by_user_id) REFERENCES users(id)        ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS offers (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    application_id  INT NOT NULL UNIQUE,
    student_id      INT NOT NULL,
    drive_id        INT NOT NULL,
    package_lpa     DECIMAL(8,2) NOT NULL CHECK (package_lpa >= 0.0),
    joining_date    DATE NOT NULL,
    status          ENUM('PENDING', 'ACCEPTED', 'DECLINED') NOT NULL,
    issued_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at    DATETIME NULL,
    CONSTRAINT fk_offer_app     FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE,
    CONSTRAINT fk_offer_student FOREIGN KEY (student_id)     REFERENCES students(id)     ON DELETE CASCADE,
    CONSTRAINT fk_offer_drive   FOREIGN KEY (drive_id)       REFERENCES drives(id)       ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS notifications (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    title           VARCHAR(150) NOT NULL,
    message         TEXT NOT NULL,
    is_read         TINYINT(1) NOT NULL DEFAULT 0,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS audit_log (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    actor_user_id   INT NULL,
    action          VARCHAR(80)  NOT NULL,
    entity_type     VARCHAR(50)  NOT NULL,
    entity_id       INT NULL,
    details         TEXT,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_students_branch      ON students(branch);
CREATE INDEX idx_students_cgpa        ON students(cgpa);
CREATE INDEX idx_drives_status        ON drives(status);
CREATE INDEX idx_drives_company       ON drives(company_id);
CREATE INDEX idx_applications_student ON applications(student_id);
CREATE INDEX idx_applications_drive   ON applications(drive_id);
CREATE INDEX idx_offers_student       ON offers(student_id);
CREATE INDEX idx_notifications_user   ON notifications(user_id, is_read);
