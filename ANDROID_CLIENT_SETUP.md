# Android Client Setup

This workspace now includes a native Android client in:

- `/Users/didintlemakhubedu/Documents/Repos/AI HEALTH AGENT/AndroidClient`

## Architecture

The project now follows this split:

- `AI HEALTH AGENT`
  Existing servlet/JSP project acting as backend, admin portal, and database layer

- `AndroidClient`
  Kotlin + Jetpack Compose mobile app for patient login, Health Connect access, and syncing vitals to the backend

## Android app flow

1. The user signs in through `POST /api/mobile/login`
2. The app loads the user profile from `GET /api/mobile/me`
3. The app reads Health Connect data from the phone
4. The app sends heart rate, body temperature when available, and blood pressure to `POST /api/mobile/health-sync`, or section summaries to `POST /api/mobile/health-section-sync`
5. The app refreshes the latest backend values and rule-based screening prediction from `GET /api/mobile/health-sync`

## Important configuration

The Android app currently uses this default backend URL:

- `https://ai-health-helper.onrender.com/`

That is the production-leaning HTTPS default used by the app build.

If you intentionally need a local backend build for development, change `BASE_URL` in a local-only branch or build variant:

- [/Users/didintlemakhubedu/Documents/Repos/AI HEALTH AGENT/AndroidClient/app/build.gradle.kts](/Users/didintlemakhubedu/Documents/Repos/AI%20HEALTH%20AGENT/AndroidClient/app/build.gradle.kts)

Do not commit an HTTP local backend URL to the production app config.

## Build notes

- The Android project uses Jetpack Compose as the UI toolkit.
- Health Connect dependency is `androidx.health.connect:connect-client:1.1.0`.
- The project is configured for modern Android tooling and expects JDK 17+ in Android Studio.

## What is implemented

- Native login screen
- Backend session-based authentication
- Profile loading
- Blank dashboard until a section is synced
- Health Connect latest-section sync button
- Demo section button for presentations
- Samsung Health section sync with heart rate, blood pressure, oxygen permission checks, and temperature only when the watch/source exposes it
- Backend rule-based screening prediction card
- Health permissions rationale screen
- Health Connect onboarding entry activity

## Verification limits in this environment

- Android Gradle build and lint have been verified in this workspace.
- Servlet compile and NetBeans Ant test have been verified in this workspace.
- Real Galaxy Watch and phone verification still requires the physical device.

## Recommended next run sequence

1. Start the servlet backend in NetBeans or GlassFish
2. Apply the SQL updates so the readings and `health_sync_sections` tables exist
3. Open `AndroidClient` in Android Studio
4. Sync Gradle with JDK 17+
5. Run the app on an emulator or Android phone
6. Sign in with a real user from your database
7. Grant Samsung Health and Health Connect permissions
8. Use automatic foreground sync or tap `Sync Samsung Health`
9. Confirm Android and the patient web dashboard show the same latest vitals and screening prediction
