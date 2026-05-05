-- Adds patient account verification status to existing databases.
-- New accounts are created as pending verification by default.

-- PostgreSQL / Supabase:
ALTER TABLE users
ADD COLUMN IF NOT EXISTS is_verified BOOLEAN NOT NULL DEFAULT FALSE;

-- MariaDB / MySQL:
-- ALTER TABLE users
-- ADD COLUMN is_verified BOOLEAN NOT NULL DEFAULT FALSE;
