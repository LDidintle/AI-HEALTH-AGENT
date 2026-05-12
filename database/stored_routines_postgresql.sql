-- PostgreSQL/Supabase stored routines for rubric DML requirements.
-- Run after database/supabase_schema.sql and any hospital/alert migrations.

BEGIN;

CREATE OR REPLACE FUNCTION sp_register_patient_account(
  p_title VARCHAR,
  p_first_name VARCHAR,
  p_surname VARCHAR,
  p_dob DATE,
  p_email VARCHAR,
  p_password_hash VARCHAR
)
RETURNS INTEGER
LANGUAGE plpgsql
AS $$
DECLARE
  v_user_id INTEGER;
BEGIN
  INSERT INTO users (title, first_name, surname, dob, email, is_verified)
  VALUES (p_title, p_first_name, p_surname, p_dob, p_email, FALSE)
  RETURNING id INTO v_user_id;

  INSERT INTO user_auth (user_id, password_hash)
  VALUES (v_user_id, p_password_hash);

  RETURN v_user_id;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_delete_patient_account(
  p_user_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
  DELETE FROM user_auth WHERE user_id = p_user_id;
  DELETE FROM users WHERE id = p_user_id;
END;
$$;

COMMIT;
