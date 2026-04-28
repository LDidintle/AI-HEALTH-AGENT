# DML Operations Demo

This project demonstrates DML operations through the web and mobile interfaces rather than requiring users to type SQL manually.

## Create

Website:
- `welcome.html` collects patient details.
- `password.jsp` collects password setup.
- `TestServlet.do` inserts into `users` and `user_auth`.

Mobile:
- `/api/mobile/register` inserts into `users` and `user_auth`.
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

## Suggested Demonstration Order

1. Create a patient account.
2. Log in as doctor/staff.
3. Open Patient Directory and search for that patient.
4. Edit the patient.
5. Sync a mobile health reading.
6. Refresh the patient/mobile dashboard to show the latest values.
7. Delete a test patient if needed.
