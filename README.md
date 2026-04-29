# SmartHealth Demo Guide

## Assignment Mapping

Design and development of a 3-tiered application architecture:
- Presentation tier: website pages and Android app.
- Application tier: Java Servlets on GlassFish.
- Data tier: MariaDB database.

Database maintenance:
- Schema export: `database/schema.sql`
- Backup script: `database/backup_health_app_db.sh`
- Restore script: `database/restore_health_app_db.sh`
- Remote-user script: `database/setup_remote_user.sql`

Build database with 3 related tables using open-source DBMS:
- DBMS: MariaDB.
- Core related tables: `users`, `user_auth`, `pulse_readings`, `temperature_readings`, `blood_pressure_readings`, `device_sync_events`.

Set up remote connection:
- Backend remote testing currently works through ngrok.
- MariaDB listens on TCP `3306`.
- Direct MariaDB remote user setup is documented in `database/setup_remote_user.sql`.

Build interface for DML operations:
- Create: website/mobile account creation and mobile health sync.
- Read: doctor/staff patient directory, search, profile pages, mobile latest readings.
- Update: doctor/staff edit patient.
- Delete: doctor/staff delete patient.

## Demo Script

1. Start MariaDB.
2. Start GlassFish and deploy `SWP_MergedProject2`.
3. Open `http://localhost:8080/SWP_MergedProject2/index.html`.
4. Create a patient account from the website.
5. Sign in as the patient and show the patient dashboard.
6. Sign in as doctor/staff:
   - Username: `healthguizer`
   - Password: configure `SMARTHEALTH_STAFF_PASSWORD` in your local environment
7. Open Patient Directory and demonstrate search.
8. Edit a patient record.
9. Demonstrate mobile API account creation/login/sync through ngrok or the Android app.
10. Show `database/schema.sql` and backup/restore scripts.

## Current Local URLs

- Website: `http://localhost:8080/SWP_MergedProject2/index.html`
- Doctor/staff login: `http://localhost:8080/SWP_MergedProject2/admin_sign.html`
- Patient login: `http://localhost:8080/SWP_MergedProject2/user_sign.html`
- Patient directory: `http://localhost:8080/SWP_MergedProject2/ViewUsersServlet.do`

## Local Deploy Command

Use this instead of starting GlassFish manually from NetBeans when you need the AI, database, and staff-login environment variables loaded:

```sh
./scripts/deploy_local_glassfish.sh .env
```

Online deployment notes are in `docs/ONLINE_DEPLOYMENT.md`.

## Docker

Build the production image locally:

```sh
docker build -t smarthealth-agent .
```

Run it with environment variables from `.env`:

```sh
docker run --rm --env-file .env -p 8081:8080 smarthealth-agent
```

Then open:

```text
http://localhost:8081/healthApp.html
```

## Current Test Account

- Patient email: `john@gmail.com`
- Patient password: `test123`

## Important Health Disclaimer

The AI suggestions are non-diagnostic wellness screening suggestions. The app does not provide medical advice, diagnose disease, or replace professional care.
