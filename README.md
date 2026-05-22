# SmartHealth AI Health Agent

## Overview

SmartHealth AI Health Agent is a student-built health monitoring application with a Java web backend, patient, staff, and hospital web portals, an Android client, database-backed health readings, automatic periodic mobile sync, and AI-assisted wellness guidance.

The app is designed to collect patient information and health readings, store them in a relational database, show recent and average readings to users, staff, and hospital users, and provide non-diagnostic wellness suggestions based on rule checks and an optional OpenAI-powered chat assistant.

## Medical Disclaimer

This project is for educational and demonstration purposes only. It does not diagnose illness, prescribe treatment, or replace a doctor, clinic, emergency service, or qualified healthcare professional. If a user has chest pain, trouble breathing, fainting, confusion, stroke symptoms, severe weakness, dangerously abnormal readings, or feels seriously unwell, they should seek urgent medical help immediately.

## Features

- Patient account creation and sign-in through the Java web app
- Staff/admin sign-in using environment-configured credentials
- Staff patient directory with create, read, update, delete, and search workflows
- Read-only hospital portal using `SMARTHEALTH_HOSPITAL_USER` and `SMARTHEALTH_HOSPITAL_PASSWORD`
- Patient profile fields for SA ID number, personal number, emergency contact, blood group, allergies, and chronic conditions
- Validation for South African phone numbers and SA ID numbers
- Validation that personal phone number and emergency contact number cannot be the same
- Database tables for users, authentication records, pulse readings, temperature readings, blood pressure readings, and device sync events
- Mobile API endpoints for registration, login, profile access, and health reading synchronization
- Android client built with Kotlin and Jetpack Compose
- Android Health Connect integration for reading heart rate, body temperature, and blood pressure records
- Galaxy Watch workflow through Samsung Health direct sync, with Health Connect as a fallback path
- Section sync mode that imports the latest 60 minutes of approved Health Connect records
- Patient sleep schedule context shared between Android and the web dashboard for safer wellness suggestions
- Demo section mode for emulator testing, with realistic section-style watch data
- Recent vital markers and trend graphs in the Android app and web patient dashboard
- Device metadata capture for synced watch/phone readings
- Doctor summary view with average vitals and rule-based prediction text
- Backend rule-based screening prediction returned to Android and the patient web dashboard
- Rule-based health insight engine in the Android client
- AI chat servlet that can call the OpenAI Responses API when an API key is configured
- Fallback wellness guidance when the AI service or API key is unavailable
- Dockerfile for containerized deployment
- Render deployment configuration
- Supabase/PostgreSQL schema and setup documentation
- MariaDB schema, backup, and restore scripts for local or classroom database demonstrations

## Tech Stack

- Java Servlets and JSP
- JDBC
- GlassFish for local Java EE development
- Tomcat 9 for Docker deployment
- MariaDB for local/classroom database setup
- PostgreSQL/Supabase for hosted deployment
- Android Kotlin
- Jetpack Compose
- Android Health Connect
- OpenAI Responses API
- Docker
- Render

## System Architecture

The project follows a 3-tier architecture:

- Presentation tier: JSP/HTML pages for patient and staff web workflows, plus a native Android client.
- Application tier: Java servlets handle authentication, patient CRUD, mobile API requests, health sync, and AI chat requests.
- Data tier: MariaDB or PostgreSQL stores patient records, password hashes, health readings, and sync logs.

```text
Patient Web / Staff Web / Hospital Portal / Android App
                |
                v
        Java Servlet Backend
                |
                v
    MariaDB or Supabase PostgreSQL
                |
                v
      Stored readings and user data

Optional:
Galaxy Watch -> Samsung Health -> Android App -> Backend sync endpoint
Galaxy Watch -> Samsung Health -> Health Connect -> Android App -> Backend sync endpoint
Android Demo Feed -> Backend sync endpoint
Backend AIChatServlet -> OpenAI Responses API -> Wellness guidance
```

## Input -> Process -> Output Flow

```text
Patient/staff/hospital input or Android Health Connect readings
        ->
Servlet validates request and reads/writes database records
        ->
Health readings are classified and stored
        ->
Web pages, mobile screens, doctor summary, and AI chat return user-facing results
```

For Android section sync/demo data:

```text
Health Connect latest-section sync or Android demo section
        ->
Android app builds a section summary with vitals and device metadata
        ->
MobileHealthSectionSyncServlet stores one section and representative latest rows
        ->
Android and web dashboards show the latest synced section values and trend graph
```

For AI chat:

```text
User message + displayed vitals + chat history
        ->
AIChatServlet builds a safety-focused prompt
        ->
OpenAI Responses API is called if configured
        ->
Short non-diagnostic wellness guidance is returned
```

## Setup

### 1. Clone the repository

```sh
git clone https://github.com/LDidintle/AI-HEALTH-AGENT.git
cd AI-HEALTH-AGENT
```

### 2. Configure environment variables

Copy the example file and fill in local values:

```sh
cp .env.example .env
```

Required values depend on whether you run locally with MariaDB or online with Supabase/PostgreSQL.

Important environment variables:

```text
OPENAI_API_KEY
SMARTHEALTH_LLM_MODEL
SMARTHEALTH_AGENT_WEB_SEARCH
SMARTHEALTH_DB_URL
SMARTHEALTH_DB_USER
SMARTHEALTH_DB_PASSWORD
SMARTHEALTH_STAFF_USER
SMARTHEALTH_STAFF_PASSWORD
```

Do not commit `.env` files or real credentials.

### 3. Database setup

For local MariaDB/classroom use, review:

```text
database/schema.sql
database/setup_remote_user.sql
database/backup_health_app_db.sh
database/restore_health_app_db.sh
```

For Supabase/PostgreSQL deployment, run:

```text
database/supabase_schema.sql
```

inside the Supabase SQL editor, then configure the hosted database environment variables.

### 4. Local GlassFish deployment

Use the helper script when you need database, staff login, and AI environment variables loaded:

```sh
./scripts/deploy_local_glassfish.sh .env
```

Common local pages:

```text
http://localhost:8080/SWP_MergedProject2/index.html
http://localhost:8080/SWP_MergedProject2/user_sign.html
http://localhost:8080/SWP_MergedProject2/admin_sign.html
http://localhost:8080/SWP_MergedProject2/hospital_sign.jsp
http://localhost:8080/SWP_MergedProject2/ViewUsersServlet.do
```

### 5. Android client

The Android client is in:

```text
AndroidClient/
```

Build a debug APK with:

```sh
cd AndroidClient
./gradlew :app:assembleDebug
```

The app supports section-based health sync:

- Real watch path: pair the Galaxy Watch with a real Android phone, sign in to the SmartHealth Android app, grant Samsung Health permissions, then let automatic periodic foreground sync run. Blood pressure can sync when Samsung Health exposes it and the watch has the required calibration/source support. Temperature is conditional and may only be available as sleep-temperature trend data on supported watch/source combinations.
- Fallback path: allow Samsung Health to share data with Health Connect, then use Health Connect section sync.
- Demo path: use the emulator and load a demo section to generate presentation data through the same section-sync workflow.

## What Works Now

- Web patient, staff/admin, and hospital portal source is present and builds.
- Android app builds, lints, stores session cookies with Android Keystore encryption, and uses HTTPS production backend config.
- Mobile sync endpoints require an authenticated session.
- Samsung Health section sync supports available watch vitals and reports unavailable temperature honestly.
- Android and web sleep schedule context syncs through the backend, with local storage as an offline fallback.
- Emergency alert evaluation, rule-based screening prediction, AI-chat fallback guidance, password hashing/reset helpers, validation, and report type behavior are covered by lightweight backend checks.
- Prediction v1 is deterministic rule-based screening, not a trained ML model or diagnosis.

## Demo Checklist

1. Start the backend with `./scripts/deploy_local_glassfish.sh .env` or deploy the Docker/Render build with the required environment variables.
2. Open the patient, staff/admin, and hospital portal pages listed above.
3. Build/install the Android app from `AndroidClient`.
4. Sign in as a patient and sync Samsung Health or load the demo section.
5. Confirm the Android dashboard and patient web dashboard show the same latest vitals and rule-based screening prediction.
6. Trigger the emergency demo flow only in a demo/test account. It demonstrates hospital assignment and alert display; it is not connected to real emergency dispatch.

## Verification

Run the local verification set:

```sh
scripts/run_backend_risk_checks.sh
scripts/run_backend_integration_checks.sh
scripts/check_mobile_session_auth.sh
scripts/check_secret_patterns.sh
cd AndroidClient
./gradlew clean app:build app:lint --warning-mode all
```

The latest verification summary is in `docs/TEST_REPORT.md`.

## Known Remaining Work

- Add full servlet/integration tests with a test database for auth, CRUD, sync, alerts, reset, and reports.
- Run a live Render/Supabase HTTPS smoke test with production environment variables.
- Capture final screenshots for the landing page, dashboards, Android sync, emergency alert flow, and AI chat.
- Migrate or reset any remaining legacy SHA-256 password hashes in real data.

### 6. Docker deployment test

```sh
docker build -t smarthealth-agent .
docker run --rm --env-file .env -p 8081:8080 smarthealth-agent
```

Then open:

```text
http://localhost:8081/healthApp.html
```

## Screenshots

Add screenshots here before using this as a portfolio project:

- Landing/sign-in page
- Patient dashboard
- Staff/admin dashboard
- Hospital portal
- Patient directory
- Android section sync and demo section screen
- Section trend graph and vital markers
- AI wellness chat

## Deployment Notes

- The root `Dockerfile` builds the Java web app and deploys it as `ROOT.war` on Tomcat 9.
- `render.yaml` defines a Render Docker web service.
- Hosted deployments should use environment variables for database credentials, staff credentials, and the OpenAI API key.
- The app should use Supabase/PostgreSQL or another hosted database online. Do not rely on a private local MariaDB address in production.
- Keep `SMARTHEALTH_AGENT_WEB_SEARCH=false` unless you have tested the AI web-search behavior and source handling.

More deployment details are in:

```text
docs/ONLINE_DEPLOYMENT.md
docs/SUPABASE_SETUP.md
docs/REMOTE_DATABASE.md
```

## Security Notes

- `.env` files and real credentials must stay out of Git.
- Database backups should not be committed because they may contain user data or password hashes.
- Staff credentials are read from environment variables.
- The AI API key is read from environment variables.
- New passwords are hashed with PBKDF2-SHA256 using a per-password salt. Legacy SHA-256 hashes are still accepted during login so older demo accounts can continue to sign in until their passwords are reset.
- Health data is sensitive. A production version should add stronger authentication, access control, audit logging, HTTPS-only deployment, rate limiting, and privacy review.
- Do not present the watch feature as continuous real-time medical monitoring. It is automatic periodic sync and can be limited by Android/Samsung battery and background restrictions.
- Do not present emergency alerts as production dispatch. Users must contact local emergency services for real emergencies.

## Future Improvements

- Migrate or reset any remaining legacy SHA-256 password hashes
- Add stronger session protection and role-based authorization checks
- Add automated tests for authentication, patient CRUD, mobile sync, and AI fallback behavior
- Add input validation helpers shared across servlets
- Add clearer user-facing error states instead of generic error pages
- Improve AI response parsing with a proper JSON parser
- Add screenshots and a stable live deployment link to this README
- Add CI checks for build and basic security scanning
