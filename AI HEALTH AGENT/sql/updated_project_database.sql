DROP DATABASE IF EXISTS health_app_db;
CREATE DATABASE health_app_db;
USE health_app_db;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(10),
    first_name VARCHAR(100) NOT NULL,
    surname VARCHAR(100) NOT NULL,
    dob DATE,
    gender VARCHAR(20),
    marital_status VARCHAR(20),
    email VARCHAR(255) NOT NULL UNIQUE,
    cell_number VARCHAR(20),
    id_number VARCHAR(13),
    emergency_contact_name VARCHAR(150),
    emergency_contact_number VARCHAR(20),
    blood_group VARCHAR(10),
    known_allergies TEXT,
    chronic_conditions TEXT,
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE devices (
    device_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    device_type VARCHAR(30) NOT NULL,
    manufacturer VARCHAR(100),
    device_model VARCHAR(100),
    platform VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_device_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_user_device
        UNIQUE (user_id, platform, device_type, manufacturer, device_model)
);

CREATE TABLE user_auth (
    auth_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_auth_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE temperature_readings (
    temp_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    device_id INT,
    temperature DECIMAL(4,2) NOT NULL,
    status VARCHAR(20),
    source VARCHAR(50) DEFAULT 'MANUAL',
    external_record_id VARCHAR(100),
    measured_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    synced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_temperature_device
        FOREIGN KEY (device_id)
        REFERENCES devices(device_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_temperature_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE pulse_readings (
    pulse_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    device_id INT,
    bpm INT NOT NULL,
    status VARCHAR(20),
    source VARCHAR(50) DEFAULT 'MANUAL',
    external_record_id VARCHAR(100),
    measured_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    synced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pulse_device
        FOREIGN KEY (device_id)
        REFERENCES devices(device_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_pulse_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE blood_pressure_readings (
    bp_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    device_id INT,
    systolic INT NOT NULL,
    diastolic INT NOT NULL,
    status VARCHAR(20),
    source VARCHAR(50) DEFAULT 'MANUAL',
    external_record_id VARCHAR(100),
    measured_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    synced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_blood_pressure_device
        FOREIGN KEY (device_id)
        REFERENCES devices(device_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_blood_pressure_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE device_sync_events (
    sync_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    device_id INT,
    source_platform VARCHAR(50) NOT NULL,
    external_record_id VARCHAR(100),
    synced_for TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sync_status VARCHAR(20) DEFAULT 'SYNCED',
    CONSTRAINT fk_sync_device
        FOREIGN KEY (device_id)
        REFERENCES devices(device_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_sync_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE emergency_alerts (
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    bpm INT,
    alert_status VARCHAR(20),
    countdown_seconds INT DEFAULT 60,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_emergency_alert_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE ambulance_notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    alert_id INT NOT NULL,
    sent_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    response_status VARCHAR(50),
    CONSTRAINT fk_ambulance_alert
        FOREIGN KEY (alert_id)
        REFERENCES emergency_alerts(alert_id)
        ON DELETE CASCADE
);

CREATE TABLE language_settings (
    language_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    language VARCHAR(20),
    CONSTRAINT fk_language_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE voice_logs (
    voice_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    command_text TEXT,
    command_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_voice_log_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE health_advice (
    advice_id INT AUTO_INCREMENT PRIMARY KEY,
    advice_type VARCHAR(20),
    message TEXT
);

INSERT INTO users (title, first_name, surname, dob, gender, marital_status, email, cell_number,
    id_number, emergency_contact_name, emergency_contact_number, blood_group,
    known_allergies, chronic_conditions, address)
VALUES
('Mr', 'John', 'Doe', '2000-01-01', 'male', 'Single', 'john@gmail.com', '0712345678',
 '0001015009087', 'Jane Doe', '0823456789', 'O+', 'None', 'None', 'Pretoria'),
('Ms', 'Lerato', 'Mokoena', '1999-06-15', 'female', 'Single', 'lerato@gmail.com', '0723456789',
 '9906150012083', 'Thabo Mokoena', '0834567890', 'A+', 'Penicillin', 'Asthma', 'Johannesburg');

INSERT INTO user_auth (user_id, password_hash)
VALUES
(1, 'hashed_password_1'),
(2, 'hashed_password_2');

INSERT INTO devices (user_id, device_type, manufacturer, device_model, platform)
VALUES
(1, 'WATCH', 'Samsung', 'Galaxy Watch 5', 'HEALTH_CONNECT'),
(2, 'WATCH', 'Samsung', 'Galaxy Watch 5', 'HEALTH_CONNECT');

INSERT INTO temperature_readings (user_id, device_id, temperature, status, source, external_record_id)
VALUES
(1, 1, 38.50, 'HIGH', 'HEALTH_CONNECT', 'sample-temp-001'),
(1, 1, 36.70, 'NORMAL', 'HEALTH_CONNECT', 'sample-temp-002'),
(2, 2, 35.90, 'LOW', 'HEALTH_CONNECT', 'sample-temp-003');

INSERT INTO pulse_readings (user_id, device_id, bpm, status, source, external_record_id)
VALUES
(1, 1, 110, 'ABNORMAL', 'HEALTH_CONNECT', 'sample-pulse-001'),
(1, 1, 72, 'NORMAL', 'HEALTH_CONNECT', 'sample-pulse-002'),
(2, 2, 45, 'CRITICAL', 'HEALTH_CONNECT', 'sample-pulse-003');

INSERT INTO blood_pressure_readings (user_id, device_id, systolic, diastolic, status, source, external_record_id)
VALUES
(1, 1, 135, 85, 'NORMAL', 'HEALTH_CONNECT', 'sample-bp-001'),
(2, 2, 148, 96, 'HIGH', 'HEALTH_CONNECT', 'sample-bp-002');

INSERT INTO emergency_alerts (user_id, bpm, alert_status, countdown_seconds)
VALUES
(2, 45, 'STARTED', 60);

INSERT INTO ambulance_notifications (alert_id, response_status)
VALUES
(1, 'PENDING');

INSERT INTO device_sync_events (user_id, device_id, source_platform, external_record_id, sync_status)
VALUES
(1, 1, 'HEALTH_CONNECT', 'sample-sync-001', 'SYNCED'),
(2, 2, 'HEALTH_CONNECT', 'sample-sync-002', 'SYNCED');

INSERT INTO language_settings (user_id, language)
VALUES
(1, 'English'),
(2, 'isiZulu');

INSERT INTO voice_logs (user_id, command_text)
VALUES
(1, 'Check my temperature'),
(2, 'Call ambulance now');

INSERT INTO health_advice (advice_type, message)
VALUES
('HIGH_TEMP', 'Drink water and monitor your temperature closely.'),
('LOW_BPM', 'Seek medical help if the low pulse rate persists.'),
('CRITICAL_BPM', 'Emergency services may be required immediately.');
