# SmartHealth AI Health Agent

## Overview

SmartHealth AI Health Agent is a student-built health monitoring application with a Java web backend, patient, staff, and hospital web portals, an Android client, database-backed health readings, live mobile sync, and AI-assisted wellness guidance.

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
- Galaxy Watch 5 workflow through Samsung Health -> Health Connect -> Android app -> backend -> web dashboard
- Section sync mode that imports the latest 60 minutes of approved Health Connect records
- Demo section mode for emulator testing, with realistic section-style watch data
- Live vital markers and trend graphs in the Android app and web patient dashboard
- Device metadata capture for synced watch/phone readings
- Doctor summary view with average vitals and rule-based prediction text
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
Galaxy Watch 5 -> Samsung Health -> Health Connect -> Android App -> Backend sync endpoint
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

- Real watch path: pair Galaxy Watch 5 with a real Android phone, allow Samsung Health to share data with Health Connect, sign in to the SmartHealth Android app, then sync the latest 60-minute section.
- Demo path: use the emulator and load a demo section to generate presentation data through the same section-sync workflow.

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
- The current password hashing helper uses SHA-256. For production systems, use a password hashing algorithm designed for authentication, such as BCrypt, Argon2, or PBKDF2 with a unique salt and appropriate work factor.
- Health data is sensitive. A production version should add stronger authentication, access control, audit logging, HTTPS-only deployment, rate limiting, and privacy review.

## Future Improvements

- Replace SHA-256 password hashing with BCrypt, Argon2, or PBKDF2
- Add stronger session protection and role-based authorization checks
- Add automated tests for authentication, patient CRUD, mobile sync, and AI fallback behavior
- Add input validation helpers shared across servlets
- Add clearer user-facing error states instead of generic error pages
- Improve AI response parsing with a proper JSON parser
- Add screenshots and a stable live deployment link to this README
- Add CI checks for build and basic security scanning
