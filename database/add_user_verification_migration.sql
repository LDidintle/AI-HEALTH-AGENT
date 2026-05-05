-- Adds patient account verification status to existing databases.
-- New accounts are created as pending verification by default.

-- PostgreSQL / Supabase:
BEGIN;

ALTER TABLE users
ADD COLUMN IF NOT EXISTS is_verified BOOLEAN NOT NULL DEFAULT FALSE;

COMMIT;

-- MariaDB / MySQL:
-- ALTER TABLE users
-- ADD COLUMN is_verified BOOLEAN NOT NULL DEFAULT FALSE;
