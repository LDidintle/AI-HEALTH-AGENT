CREATE DATABASE IF NOT EXISTS health_app_db;
USE health_app_db;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(10),
    first_name VARCHAR(100) NOT NULL,
    surname VARCHAR(100) NOT NULL,
    dob DATE,
    gender VARCHAR(20),
    marital_status VARCHAR(20),
    email VARCHAR(255) NOT NULL UNIQUE,
    cell_number VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS devices (
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

CREATE TABLE IF NOT EXISTS user_auth (
    auth_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_auth_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS temperature_readings (
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

CREATE TABLE IF NOT EXISTS pulse_readings (
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

CREATE TABLE IF NOT EXISTS blood_pressure_readings (
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

CREATE TABLE IF NOT EXISTS device_sync_events (
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

CREATE TABLE IF NOT EXISTS emergency_alerts (
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

CREATE TABLE IF NOT EXISTS ambulance_notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    alert_id INT NOT NULL,
    sent_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    response_status VARCHAR(50),
    CONSTRAINT fk_ambulance_alert
        FOREIGN KEY (alert_id)
        REFERENCES emergency_alerts(alert_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS language_settings (
    language_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    language VARCHAR(20),
    CONSTRAINT fk_language_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS voice_logs (
    voice_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    command_text TEXT,
    command_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_voice_log_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS health_advice (
    advice_id INT AUTO_INCREMENT PRIMARY KEY,
    advice_type VARCHAR(20),
    message TEXT
);

INSERT INTO health_advice (advice_type, message)
SELECT 'HIGH_TEMP', 'Drink water and monitor your temperature closely.'
WHERE NOT EXISTS (
    SELECT 1 FROM health_advice WHERE advice_type = 'HIGH_TEMP'
);

INSERT INTO health_advice (advice_type, message)
SELECT 'LOW_BPM', 'Seek medical help if the low pulse rate persists.'
WHERE NOT EXISTS (
    SELECT 1 FROM health_advice WHERE advice_type = 'LOW_BPM'
);

INSERT INTO health_advice (advice_type, message)
SELECT 'CRITICAL_BPM', 'Emergency services may be required immediately.'
WHERE NOT EXISTS (
    SELECT 1 FROM health_advice WHERE advice_type = 'CRITICAL_BPM'
);

INSERT INTO users (first_name, surname, email, title, dob, gender, marital_status, cell_number, address)
SELECT 'John', 'Doe', 'john@gmail.com', 'Mr', '2000-01-01', 'male', 'Single', '0712345678', 'Test Address'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'john@gmail.com'
);

INSERT INTO devices (user_id, device_type, manufacturer, device_model, platform)
SELECT id, 'WATCH', 'Samsung', 'Galaxy Watch 5', 'HEALTH_CONNECT'
FROM users
WHERE email = 'john@gmail.com'
  AND NOT EXISTS (
      SELECT 1
      FROM devices
      WHERE user_id = users.id
        AND platform = 'HEALTH_CONNECT'
        AND device_type = 'WATCH'
        AND manufacturer = 'Samsung'
        AND device_model = 'Galaxy Watch 5'
  );

INSERT INTO temperature_readings (user_id, temperature, status)
SELECT id, 38.50, 'HIGH'
FROM users
WHERE email = 'john@gmail.com'
  AND NOT EXISTS (
      SELECT 1
      FROM temperature_readings
      WHERE user_id = users.id
        AND temperature = 38.50
        AND status = 'HIGH'
  );

INSERT INTO pulse_readings (user_id, bpm, status, source)
SELECT id, 98, 'NORMAL', 'HEALTH_CONNECT'
FROM users
WHERE email = 'john@gmail.com'
  AND NOT EXISTS (
      SELECT 1
      FROM pulse_readings
      WHERE user_id = users.id
        AND bpm = 98
        AND status = 'NORMAL'
  );

INSERT INTO blood_pressure_readings (user_id, systolic, diastolic, status, source)
SELECT id, 135, 85, 'NORMAL', 'HEALTH_CONNECT'
FROM users
WHERE email = 'john@gmail.com'
  AND NOT EXISTS (
      SELECT 1
      FROM blood_pressure_readings
      WHERE user_id = users.id
        AND systolic = 135
        AND diastolic = 85
  );
