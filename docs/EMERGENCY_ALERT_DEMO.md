# Emergency Alert Demo Plan

Use this flow to demonstrate the hospital registration, alert assignment, and phone notification features.

## Demo Accounts

Run the matching seed script before the demo:

- PostgreSQL/Supabase: `database/demo_hospital_seed_postgresql.sql`
- MariaDB: `database/demo_hospital_seed_mariadb.sql`

Seeded hospital:

- Email: `demo.hospital@smarthealth.local`
- Password: `Demo@12345`
- Service area: `Pretoria`

Seeded patient:

- Email: `demo.patient@smarthealth.local`
- Password: `Patient@12345`
- Address: `Pretoria CBD, Pretoria`

The patient address contains `Pretoria`, so abnormal readings are assigned to the demo hospital automatically.

## What To Show Sir

1. Open the hospital portal from the web home page.
2. Show that hospitals can register with a service area.
3. Sign in as the demo hospital.
4. Show the patient-alert portal. It may be empty before the emergency reading.
5. Open the Android app and sign in as the demo patient.
6. Tap `Trigger Emergency Demo Alert`.
7. Point out the phone emergency notification card:
   - alert status
   - assigned hospital
   - heart rate / emergency detail
8. Go back to the hospital portal and refresh.
9. Show that the demo patient appears in the hospital alert list.
10. Open the patient details from the hospital portal and show the summary.
11. Open `Reports > Emergency Alerts` or the hospital `Emergency Report`.
12. Export CSV to show the report-export requirement.

## Backup Browser/API Proof

If the Android UI is slow, use this endpoint after signing in or with the seeded patient email:

```text
/api/mobile/alerts?email=demo.patient@smarthealth.local
```

Expected result after the emergency demo:

- `hasAlert` is `true`
- `hospitalName` is `SmartHealth Pretoria Demo Hospital`
- `status` is `CRITICAL`

## Why This Satisfies The Feature

- The phone sends abnormal vitals through the existing sync endpoint.
- The backend stores readings in the reading tables.
- `VitalAlertEvaluator` creates an emergency alert when thresholds are abnormal.
- The alert is assigned to a hospital by matching patient address to hospital service area.
- The phone checks `/api/mobile/alerts` and shows the latest alert notification.
- The hospital portal and emergency report show the same database-backed alert.
