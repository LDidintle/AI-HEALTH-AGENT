# Android Client Setup

This workspace now includes a native Android client in:

- `/Users/didintlemakhubedu/Documents/223451918/SWP316D/SWP_MergedProject 2/AndroidClient`

## Architecture

The project now follows this split:

- `SWP_MergedProject2`
  Existing servlet/JSP project acting as backend, admin portal, and database layer

- `AndroidClient`
  Kotlin + Jetpack Compose mobile app for patient login, Health Connect access, and syncing vitals to the backend

## Android app flow

1. The user signs in through `POST /api/mobile/login`
2. The app loads the user profile from `GET /api/mobile/me`
3. The app reads Health Connect data from the phone
4. The app sends heart rate, body temperature, and blood pressure to `POST /api/mobile/health-sync`
5. The app refreshes the latest backend values from `GET /api/mobile/health-sync`

## Important configuration

The Android app currently uses this default backend URL:

- `http://10.0.2.2:8080/SWP_MergedProject2/`

That works for the Android emulator when the backend is running on the same computer.

If you run the app on a physical phone, change `BASE_URL` in:

- [/Users/didintlemakhubedu/Documents/223451918/SWP316D/SWP_MergedProject 2/AndroidClient/app/build.gradle.kts](/Users/didintlemakhubedu/Documents/223451918/SWP316D/SWP_MergedProject%202/AndroidClient/app/build.gradle.kts)

Use your computer's LAN IP address, for example:

- `http://192.168.x.x:8080/SWP_MergedProject2/`

## Build notes

- The Android project uses Jetpack Compose as the UI toolkit.
- Health Connect dependency is `androidx.health.connect:connect-client:1.1.0`.
- The project is configured for modern Android tooling and expects JDK 17+ in Android Studio.

## What is implemented

- Native login screen
- Backend session-based authentication
- Profile loading
- Latest vital reading display
- Health Connect sync button
- Manual sample sync button for demos
- Health permissions rationale screen
- Health Connect onboarding entry activity

## Verification limits in this environment

- I could not run an Android build here because this environment does not have Gradle or the Android SDK installed.
- I also could not compile the servlet project here because `ant` is not installed.

## Recommended next run sequence

1. Start the servlet backend in NetBeans or GlassFish
2. Apply the SQL updates so the new readings tables exist
3. Open `AndroidClient` in Android Studio
4. Sync Gradle with JDK 17+
5. Run the app on an emulator or Android phone
6. Sign in with a real user from your database
7. Grant Health Connect permissions
8. Tap `Sync From Health Connect`
