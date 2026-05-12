-- MariaDB stored routines for rubric DML requirements.
-- Run after database/schema.sql or AI HEALTH AGENT/sql/smart_health_monitor_schema.sql.

DELIMITER //

DROP PROCEDURE IF EXISTS sp_register_patient_account //
CREATE PROCEDURE sp_register_patient_account(
  IN p_title VARCHAR(10),
  IN p_first_name VARCHAR(100),
  IN p_surname VARCHAR(100),
  IN p_dob DATE,
  IN p_email VARCHAR(255),
  IN p_password_hash VARCHAR(255),
  OUT p_user_id INT
)
BEGIN
  INSERT INTO users (title, first_name, surname, dob, email, is_verified)
  VALUES (p_title, p_first_name, p_surname, p_dob, p_email, FALSE);

  SET p_user_id = LAST_INSERT_ID();

  INSERT INTO user_auth (user_id, password_hash)
  VALUES (p_user_id, p_password_hash);
END //

DROP PROCEDURE IF EXISTS sp_delete_patient_account //
CREATE PROCEDURE sp_delete_patient_account(
  IN p_user_id INT
)
BEGIN
  DELETE FROM user_auth WHERE user_id = p_user_id;
  DELETE FROM users WHERE id = p_user_id;
END //

DELIMITER ;
