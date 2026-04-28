# SmartHealth Test Report

Test date: 2026-04-25

## Environment

- Web server: GlassFish
- Database: MariaDB
- Database name: `health_app_db`
- Android build: Gradle debug APK
- Public tunnel for testing: ngrok

## Results

| Area | Test | Result |
| --- | --- | --- |
| Website | Landing page returns HTTP 200 | Pass |
| Website | Patient login page returns HTTP 200 | Pass |
| Website | Registration page returns HTTP 200 | Pass |
| Website | Doctor/staff login page returns HTTP 200 | Pass |
| Website | Patient login redirects to dashboard | Pass |
| Website | Doctor/staff login redirects to dashboard | Pass |
| Website | Account creation redirects to confirmation page | Pass |
| Doctor/staff | Patient directory loads | Pass |
| Doctor/staff | Patient search UI exists | Pass |
| Database | Expected tables exist | Pass |
| Database | Backup export created | Pass |
| Mobile API | Register through ngrok | Pass |
| Mobile API | Login through ngrok | Pass |
| Mobile API | Sync health readings through ngrok | Pass |
| Mobile API | Latest health readings through ngrok | Pass |
| Android | Debug APK builds | Pass |
| Remote DB | MariaDB listens on TCP 3306 | Pass |
| Remote DB | LAN-IP connection reaches MariaDB | Pass |
| Remote DB | Limited remote user created for `10.0.0.%` | Pass |
| Remote DB | Remote user can query through LAN IP | Pass |
| Firewall | macOS firewall status inspected | Enabled |
| Firewall | MariaDB incoming access inspected | Permitted |
| Firewall | Java/GlassFish incoming access inspected | Permitted |

## Known Gaps

- Real Galaxy Watch 5 end-to-end test still requires the physical watch and phone pairing.
- Remote DB test from a second physical device is still recommended.
- macOS firewall changes would require explicit approval if future changes are needed.
- Production deployment still needs HTTPS hosting and a managed/private database.
