# Clinical Notes And Screening Summary Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace misleading doctor-summary text with the shared prediction engine output and add editable patient-level clinical notes for staff and hospital detail pages.

**Architecture:** Keep the existing patient-details flow, but load one stored clinical note per patient through a dedicated service and save it through a new servlet with role-aware authorization. Reuse the existing `HealthRiskPredictionService` for the portal screening summary so the hospital list, patient details, and patient readings API share the same prediction language.

**Tech Stack:** Java Servlets, JSP, JDBC, MariaDB/PostgreSQL migration SQL, Derby-based integration checks.

---

### Task 1: Add failing backend tests

**Files:**
- Modify: `AI HEALTH AGENT/test/BackendIntegrationChecks.java`
- Modify: `AI HEALTH AGENT/test/BackendRiskChecks.java`
- Modify: `scripts/run_backend_integration_checks.sh`

- [ ] Add integration checks for clinical note save, update, load, and clear behavior.
- [ ] Add role-access checks for the new clinical note servlet route.
- [ ] Run `./scripts/run_backend_integration_checks.sh` and `./scripts/run_backend_risk_checks.sh` to verify the new expectations fail before implementation.

### Task 2: Add schema and storage service

**Files:**
- Create: `AI HEALTH AGENT/src/java/za/ac/tut/model/ClinicalNote.java`
- Create: `AI HEALTH AGENT/src/java/za/ac/tut/util/ClinicalNoteService.java`
- Create: `database/clinical_notes_migration_mariadb.sql`
- Create: `database/clinical_notes_migration_postgresql.sql`

- [ ] Add a patient-level `clinical_notes` table with one note row per patient.
- [ ] Implement JDBC load/save/delete note behavior with note-length validation and last-updated metadata.
- [ ] Re-run the failing integration test subset until storage behavior passes.

### Task 3: Add note editing route and prediction unification

**Files:**
- Create: `AI HEALTH AGENT/src/java/za/ac/tut/web/UpdateClinicalNoteServlet.java`
- Modify: `AI HEALTH AGENT/src/java/za/ac/tut/util/PatientSummaryService.java`
- Modify: `AI HEALTH AGENT/src/java/za/ac/tut/util/RoleAccessPolicy.java`
- Modify: `AI HEALTH AGENT/web/WEB-INF/web.xml`

- [ ] Add a servlet that saves notes for admins and hospital users with patient-access checks.
- [ ] Switch `PatientSummaryService` prediction text to the shared `HealthRiskPredictionService`.
- [ ] Register the new route in role access and `web.xml`.
- [ ] Run risk and integration checks again.

### Task 4: Update staff and hospital detail pages

**Files:**
- Modify: `AI HEALTH AGENT/src/java/za/ac/tut/web/ReadUserServlet.java`
- Modify: `AI HEALTH AGENT/src/java/za/ac/tut/web/HospitalPatientDetailsServlet.java`
- Modify: `AI HEALTH AGENT/web/read_user_result.jsp`
- Modify: `AI HEALTH AGENT/web/hospital_patients.jsp`
- Modify: `AI HEALTH AGENT/web/page-ui.css`

- [ ] Load clinical notes into both detail-page entry points.
- [ ] Rename summary labels so they describe generated screening output rather than doctor-authored notes.
- [ ] Add the editable clinical notes form and success/error states to the details page.
- [ ] Run a servlet compile/build verification command.

### Task 5: Verify end to end

**Files:**
- Modify: `scripts/run_backend_integration_checks.sh`

- [ ] Run `./scripts/run_backend_risk_checks.sh`.
- [ ] Run `./scripts/run_backend_integration_checks.sh`.
- [ ] Run a focused `javac` compile over the changed servlet/util/model files.
- [ ] If runtime database migration is not safe to apply locally, report that limitation explicitly instead of claiming live UI verification.
