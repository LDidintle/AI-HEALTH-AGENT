-- Stores patient sleep-window preferences used by Android and web suggestion context.

CREATE TABLE IF NOT EXISTS patient_context_settings (
  user_id INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  sleep_start VARCHAR(5) NOT NULL,
  sleep_end VARCHAR(5) NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE patient_context_settings ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON TABLE patient_context_settings FROM anon, authenticated;
