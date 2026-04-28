-- Run as a MariaDB administrator after replacing the placeholders.
-- This creates a limited remote user for the application database only.
-- Prefer a LAN subnet host such as '10.0.0.%' instead of '%' for safety.

CREATE USER IF NOT EXISTS 'health_app_remote'@'10.0.0.%'
IDENTIFIED BY 'CHANGE_THIS_STRONG_PASSWORD';

GRANT SELECT, INSERT, UPDATE, DELETE
ON health_app_db.*
TO 'health_app_remote'@'10.0.0.%';

FLUSH PRIVILEGES;

SHOW GRANTS FOR 'health_app_remote'@'10.0.0.%';
