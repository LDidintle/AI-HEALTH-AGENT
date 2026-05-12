-- Demo hospital and patient seed for the emergency-alert presentation.
-- Passwords:
--   hospital email: demo.hospital@smarthealth.local
--   hospital password: Demo@12345
--   patient email: demo.patient@smarthealth.local
--   patient password: Patient@12345

BEGIN;

INSERT INTO hospitals (name, email, phone, service_area, address, active)
SELECT 'SmartHealth Pretoria Demo Hospital',
       'demo.hospital@smarthealth.local',
       '0123456789',
       'Pretoria',
       '123 Demo Street, Pretoria',
       TRUE
WHERE NOT EXISTS (
  SELECT 1 FROM hospitals WHERE email = 'demo.hospital@smarthealth.local'
);

INSERT INTO hospital_auth (hospital_id, password_hash)
SELECT h.hospital_id,
       '11eecee636899469c8f0db8c75282b38a997496a4bf6a2babc77e4480b6f9bea'
FROM hospitals h
WHERE h.email = 'demo.hospital@smarthealth.local'
  AND NOT EXISTS (
    SELECT 1 FROM hospital_auth ha WHERE ha.hospital_id = h.hospital_id
  );

INSERT INTO users (
  title, first_name, surname, dob, gender, marital_status, email, cell_number,
  id_number, emergency_contact_name, emergency_contact_number, blood_group,
  known_allergies, chronic_conditions, address, is_verified
)
SELECT 'Patient', 'Demo', 'Patient', DATE '2001-05-15', 'Female', 'Single',
       'demo.patient@smarthealth.local', '0712345678', '0105150000000',
       'Demo Guardian', '0823456789', 'O+', 'None', 'Hypertension monitoring',
       'Pretoria CBD, Pretoria', TRUE
WHERE NOT EXISTS (
  SELECT 1 FROM users WHERE email = 'demo.patient@smarthealth.local'
);

INSERT INTO user_auth (user_id, password_hash)
SELECT u.id,
       '87620dfa4341eb12297901bfbb41857d6e88280e6519c12aa2346ce1bebe32b9'
FROM users u
WHERE u.email = 'demo.patient@smarthealth.local'
  AND NOT EXISTS (
    SELECT 1 FROM user_auth ua WHERE ua.user_id = u.id
  );

COMMIT;
