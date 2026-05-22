-- Demo hospital and patient seed for the emergency-alert presentation.
-- Passwords:
--   hospital email: arcadia.ridge@smarthealth.local
--   hospital password: Demo@12345
--   patient email: demo.patient@smarthealth.local
--   patient password: Patient@12345

START TRANSACTION;

INSERT INTO hospitals (name, email, phone, service_area, address, active)
SELECT 'Arcadia Ridge Medical Centre',
       'arcadia.ridge@smarthealth.local',
       '0127420185',
       'Pretoria',
       '421 Park Street, Arcadia, Pretoria, 0083',
       TRUE
WHERE NOT EXISTS (
  SELECT 1 FROM hospitals WHERE email = 'arcadia.ridge@smarthealth.local'
);

INSERT INTO hospital_auth (hospital_id, password_hash)
SELECT h.hospital_id,
       '11eecee636899469c8f0db8c75282b38a997496a4bf6a2babc77e4480b6f9bea'
FROM hospitals h
WHERE h.email = 'arcadia.ridge@smarthealth.local'
  AND NOT EXISTS (
    SELECT 1 FROM hospital_auth ha WHERE ha.hospital_id = h.hospital_id
  );

INSERT INTO users (
  title, first_name, surname, dob, gender, marital_status, email, cell_number,
  id_number, emergency_contact_name, emergency_contact_number, blood_group,
  known_allergies, chronic_conditions, address, is_verified
)
SELECT 'Patient', 'Naledi', 'Mokoena', '2001-05-15', 'Female', 'Single',
       'demo.patient@smarthealth.local', '0712345678', '0105150000000',
       'Thabo Mokoena', '0823456789', 'O+', 'None', 'Hypertension monitoring',
       '296 Pretorius Street, Pretoria Central, Pretoria, 0002', TRUE
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

INSERT INTO emergency_alerts (user_id, bpm, alert_status, countdown_seconds, created_at)
SELECT u.id, 138, 'CRITICAL', 0, CURRENT_TIMESTAMP
FROM users u
WHERE u.email = 'demo.patient@smarthealth.local'
  AND NOT EXISTS (
    SELECT 1 FROM emergency_alerts ea
    WHERE ea.user_id = u.id
      AND ea.bpm = 138
      AND ea.alert_status = 'CRITICAL'
  );

INSERT INTO hospital_alert_assignments (alert_id, hospital_id, status)
SELECT ea.alert_id, h.hospital_id, 'ASSIGNED'
FROM emergency_alerts ea
JOIN users u ON u.id = ea.user_id
JOIN hospitals h ON h.email = 'arcadia.ridge@smarthealth.local'
WHERE u.email = 'demo.patient@smarthealth.local'
  AND ea.bpm = 138
  AND ea.alert_status = 'CRITICAL'
  AND NOT EXISTS (
    SELECT 1 FROM hospital_alert_assignments haa
    WHERE haa.alert_id = ea.alert_id
      AND haa.hospital_id = h.hospital_id
  );

INSERT INTO ambulance_notifications (alert_id, response_status)
SELECT ea.alert_id, 'DISPATCHED'
FROM emergency_alerts ea
JOIN users u ON u.id = ea.user_id
WHERE u.email = 'demo.patient@smarthealth.local'
  AND ea.bpm = 138
  AND ea.alert_status = 'CRITICAL'
  AND NOT EXISTS (
    SELECT 1 FROM ambulance_notifications an WHERE an.alert_id = ea.alert_id
  );

COMMIT;
