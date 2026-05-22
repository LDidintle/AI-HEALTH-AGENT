# SmartHealth Test Report

Test date: 2026-05-21

## Environment

- Backend: Java Servlet/JSP project built with NetBeans Ant
- Android: Kotlin/Jetpack Compose app built with Gradle 8.13
- Database targets: MariaDB for local/classroom use, PostgreSQL/Supabase for hosted use
- Scope of this report: local code, compile, static, and lightweight behavior checks

## Automated Checks Run

| Area | Check | Result |
| --- | --- | --- |
| Backend compile | Direct `javac` compile of servlet/backend sources with servlet API classpath | Pass |
| Backend Ant | `/Applications/Apache NetBeans.app/Contents/Resources/netbeans/extide/ant/bin/ant test` | Pass |
| Backend risk harness | `scripts/run_backend_risk_checks.sh` | Pass |
| Backend integration harness | `scripts/run_backend_integration_checks.sh` with local Derby test database | Pass |
| Auth/session | Role marking, current role, and 30 minute session timeout behavior | Pass |
| Patient validation | SA mobile number normalization, ID shape, DOB bounds, same-phone check | Pass |
| Password security | PBKDF2 hashing, legacy SHA-256 verification, malformed hash rejection, password policy | Pass |
| Password reset | Demo OTP hidden by default and only visible when explicitly enabled | Pass |
| Rate limiting | Repeated sensitive action attempts are blocked after configured limit | Pass |
| Emergency alerts | Critical BP, fever plus fast pulse, normal-vitals no-alert behavior | Pass |
| Reports | Report type normalization and hospital alerts-only behavior | Pass |
| Integration reports | Alert report loads against test database | Pass |
| Prediction | Normal, high BP, urgent BP, fever plus pulse, missing data, repeated abnormal sections | Pass |
| Prediction safety | API identifies `RULE_BASED_SCREENING_V1` and includes non-diagnostic/non-ML disclaimer | Pass |
| Mobile endpoint auth | `scripts/check_mobile_session_auth.sh` forbidden old `?email=` patterns | Pass |
| Secret hygiene | `scripts/check_secret_patterns.sh` high-confidence secret scan | Pass |
| Diff hygiene | `git diff --check` | Pass |
| Android | `./gradlew clean app:build app:lint --warning-mode all` | Pass |

## Manual/Runtime Checks

| Area | Check | Result |
| --- | --- | --- |
| Web deployment | WAR deployed to local GlassFish foreground run | Pass |
| Web smoke | Landing page, patient sign-in, and patient dashboard returned HTTP 200 during foreground run | Pass |
| Android device | Install/run on real phone with Galaxy Watch | Not run in this environment |
| Docker | Build/run local Docker image | Not run: Docker daemon was not running |
| Live deployment | Render/Supabase HTTPS smoke test | Not run: requires live deployment access and configured environment |
| Live HTTPS | `https://ai-health-helper.onrender.com/` returned HTTP 200 | Pass |
| Live patient page | `https://ai-health-helper.onrender.com/healthApp.html` returned HTTP 200 | Pass |
| Live mobile auth | `/api/mobile/health-sync` rejected unauthenticated read | Pass |
| Supabase security advisor | Critical `public.devices` RLS-disabled issue identified; repo migration added | Pending live approval |

## Remaining Gaps

- Run full browser screenshots from a signed-in web session and Android Studio/device. Quick Look thumbnail generation was not reliable for JSP/runtime pages.
- Apply `database/supabase_rls_hardening.sql` to the live Supabase project after confirming that blocking public `devices` access will not break any direct Supabase client usage.
- Confirm OpenAI key behavior in production, or document the fallback-only demo mode.
- Capture final screenshots for landing page, patient dashboard, staff dashboard, hospital portal, Android sync, emergency alert, and AI chat.
- Run the real Galaxy Watch/phone smoke test for Samsung Health heart rate and blood pressure sync. Temperature may remain unavailable because the test watch/source does not expose a temperature option.
