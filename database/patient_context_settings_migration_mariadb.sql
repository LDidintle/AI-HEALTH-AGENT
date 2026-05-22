-- Stores patient sleep-window preferences used by Android and web suggestion context.

CREATE TABLE IF NOT EXISTS patient_context_settings (
  user_id INT NOT NULL,
  sleep_start VARCHAR(5) NOT NULL,
  sleep_end VARCHAR(5) NOT NULL,
  updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id),
  CONSTRAINT fk_patient_context_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
