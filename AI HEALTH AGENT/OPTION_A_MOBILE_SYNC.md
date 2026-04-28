# Option A Mobile Sync Setup

This project now supports a phone-first health data flow:

`Galaxy Watch 5 -> Samsung Health on phone -> Health Connect or Samsung Health Data SDK -> Android phone app -> this web backend`

## What the backend now supports

- `POST /api/mobile/login`
Authenticates the Android user and starts a backend session.

- `GET /api/mobile/me`
Returns the logged-in user's profile for the Android app.

- `POST /api/mobile/logout`
Ends the Android user's backend session.

- `GET /ReadingServlet.do`
Returns the latest heart rate, temperature, and blood pressure for the logged-in web user.

- `POST /api/mobile/health-sync`
Allows the Android phone app to push synced health readings into this database.

- `GET /api/mobile/health-sync?email=user@example.com`
Returns the latest stored readings for a specific user. This is useful for testing the mobile bridge.

## Mobile POST parameters

- `email` required
If the user is already logged in through `/api/mobile/login`, `email` may be omitted and the session user is used.
- `source` optional, defaults to `HEALTH_CONNECT`
- `heartRate` optional integer
- `temperature` optional decimal
- `systolic` optional integer
- `diastolic` optional integer
- `recordedAt` optional timestamp string
- `externalRecordId` optional unique id from the phone app

At least one of the following must be supplied:

- `heartRate`
- `temperature`
- `systolic` and `diastolic`

## Example request

```bash
curl -X POST http://localhost:8080/SWP_MergedProject2/api/mobile/login \
  -d "email=john@gmail.com" \
  -d "password=your-password"
```

```bash
curl -X POST http://localhost:8080/SWP_MergedProject2/api/mobile/health-sync \
  -d "email=john@gmail.com" \
  -d "source=HEALTH_CONNECT" \
  -d "heartRate=82" \
  -d "temperature=36.90" \
  -d "systolic=126" \
  -d "diastolic=81"
```

## Recommended Android next step

Build a small Android app that:

1. Logs the user in with `/api/mobile/login`.
2. Loads the user profile from `/api/mobile/me`.
3. Reads the signed-in user's health data from Health Connect.
4. Maps those values to this backend endpoint.
5. Sends updates on app open, manual refresh, or a background worker.

For the first version, send:

- heart rate
- temperature
- blood pressure if available on the phone side

## Notes

- This backend still uses servlet form-style requests instead of JSON bodies to keep the integration simple.
- The health dashboard now fetches the newest server-side readings instead of only showing hard-coded sample values.
