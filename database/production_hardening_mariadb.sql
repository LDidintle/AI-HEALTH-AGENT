-- Production-leaning hardening layer for SmartHealth MariaDB.
-- Safe to run after the base schema and watch-device migrations.

CREATE TABLE IF NOT EXISTS schema_migrations (
  version VARCHAR(80) PRIMARY KEY,
  applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_events (
  audit_id INT AUTO_INCREMENT PRIMARY KEY,
  actor_user_id INT NULL,
  actor_role VARCHAR(30),
  action VARCHAR(80) NOT NULL,
  target_type VARCHAR(80),
  target_id VARCHAR(80),
  outcome VARCHAR(30),
  detail TEXT,
  ip_address VARCHAR(80),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_audit_actor_user FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_events_created ON audit_events(created_at);
CREATE INDEX idx_audit_events_actor ON audit_events(actor_user_id, created_at);

ALTER TABLE emergency_alerts
  ADD COLUMN IF NOT EXISTS lifecycle_status VARCHAR(30) DEFAULT 'CREATED';

CREATE TABLE IF NOT EXISTS alert_events (
  event_id INT AUTO_INCREMENT PRIMARY KEY,
  alert_id INT NOT NULL,
  from_status VARCHAR(30),
  to_status VARCHAR(30) NOT NULL,
  actor_role VARCHAR(30),
  actor_id VARCHAR(80),
  note TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_alert_events_alert FOREIGN KEY (alert_id) REFERENCES emergency_alerts(alert_id) ON DELETE CASCADE
);

CREATE INDEX idx_alert_events_alert ON alert_events(alert_id, created_at);

CREATE TABLE IF NOT EXISTS patient_consents (
  consent_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  consent_type VARCHAR(60) NOT NULL,
  consent_version VARCHAR(30) NOT NULL,
  accepted BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_patient_consents_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_patient_consents_user_type ON patient_consents(user_id, consent_type, created_at);

CREATE TABLE IF NOT EXISTS device_capabilities (
  capability_id INT AUTO_INCREMENT PRIMARY KEY,
  device_id INT NULL,
  source VARCHAR(80),
  heart_rate_supported BOOLEAN DEFAULT FALSE,
  blood_pressure_supported BOOLEAN DEFAULT FALSE,
  sleep_temperature_supported BOOLEAN DEFAULT FALSE,
  sleep_temperature_trend_only BOOLEAN DEFAULT FALSE,
  activity_supported BOOLEAN DEFAULT FALSE,
  caveat TEXT,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_device_capabilities_device FOREIGN KEY (device_id) REFERENCES devices(device_id) ON DELETE CASCADE
);

CREATE INDEX idx_device_capabilities_device ON device_capabilities(device_id, updated_at);

INSERT IGNORE INTO schema_migrations (version)
VALUES ('2026-05-24-production-hardening');
