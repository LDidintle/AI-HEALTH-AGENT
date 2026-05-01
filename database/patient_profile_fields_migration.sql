-- Adds patient identity, emergency contact, and clinical context fields.
-- Run once on existing Supabase/PostgreSQL databases before deploying the updated registration flow.

BEGIN;

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS id_number VARCHAR(13),
  ADD COLUMN IF NOT EXISTS emergency_contact_name VARCHAR(150),
  ADD COLUMN IF NOT EXISTS emergency_contact_number VARCHAR(20),
  ADD COLUMN IF NOT EXISTS blood_group VARCHAR(10),
  ADD COLUMN IF NOT EXISTS known_allergies TEXT,
  ADD COLUMN IF NOT EXISTS chronic_conditions TEXT;

COMMIT;
