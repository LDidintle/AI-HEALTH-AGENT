# DML Operations Demo

This project demonstrates DML operations through the web and mobile interfaces rather than requiring users to type SQL manually.

## Create

Website:
- `welcome.html` collects patient details.
- `password.jsp` collects password setup.
- `TestServlet.do` calls `sp_register_patient_account`, which inserts into `users` and `user_auth`.

Mobile:
- `/api/mobile/register` calls `sp_register_patient_account`, which inserts into `users` and `user_auth`.
- `/api/mobile/health-sync` inserts into `pulse_readings`, `temperature_readings`, `blood_pressure_readings`, and `device_sync_events`.

## Read

Website:
- `ViewUsersServlet.do` reads all patients for the Patient Directory.
- `ReadUserServlet.do` reads one patient by email.

Mobile:
- `/api/mobile/login` reads `users` and `user_auth`.
- `/api/mobile/me` reads the current profile.
- `/api/mobile/health-sync` with `GET` reads latest synced vitals.

## Update

Website:
- `EditUserServlet.do` loads a patient.
- `UpdateUserServlet.do` updates patient profile fields in `users`.

## Delete

Website:
- `DeleteUserServlet.do` deletes selected patient records from `users`.
- `DeleteUserServlet.do` calls `sp_delete_patient_account`, which deletes from `user_auth` and `users`.

## Stored Procedures

Run the matching routine script before marking the stored-procedure requirement:

- PostgreSQL/Supabase: `database/stored_routines_postgresql.sql`
- MariaDB: `database/stored_routines_mariadb.sql`

The application calls these routines first for patient account creation and deletion. If an older development database has not been migrated yet, the Java service falls back to the same transaction-safe DML so existing demos do not fail unexpectedly.

## Suggested Demonstration Order

1. Create a patient account.
2. Log in as doctor/staff.
3. Open Patient Directory and search for that patient.
4. Edit the patient.
5. Sync a mobile health reading.
6. Refresh the patient/mobile dashboard to show the latest values.
7. Delete a test patient if needed.
