-- Adds editable patient-level clinical notes for staff and hospital review.
-- Safe to run after the base schema and doctor/hospital migrations.

CREATE TABLE IF NOT EXISTS schema_migrations (
  version VARCHAR(80) PRIMARY KEY,
  applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS clinical_notes (
  note_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  note_text TEXT NOT NULL,
  updated_by_role VARCHAR(30),
  updated_by_actor VARCHAR(80),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_clinical_notes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT uq_clinical_notes_user UNIQUE (user_id)
);

CREATE INDEX idx_clinical_notes_updated ON clinical_notes(updated_at);

INSERT IGNORE INTO schema_migrations (version)
VALUES ('2026-06-16-clinical-notes');
