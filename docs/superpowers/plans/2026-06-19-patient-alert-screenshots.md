# Patient Alert Screenshots Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build patient-side web and Android alert states that produce real app screenshots for the pitch deck, demo video, funding summary, and clinic pilot proposal.

**Architecture:** Keep the existing backend alert contract. The web dashboard renders `activeAlert` from `ReadingServlet.do`; Android renders `activeAlert` from `/api/mobile/alerts`. Both clients show a persistent alert card and a full-screen emergency dialog for a fresh critical/demo alert.

**Tech Stack:** Java Servlets/JSP, browser JavaScript/CSS, Android Kotlin, Jetpack Compose.

---

### Task 1: Test Web Alert Formatting

**Files:**
- Create: `AI HEALTH AGENT/test/patient_alert_ui_test.js`
- Modify: `AI HEALTH AGENT/web/script_2.js`

- [x] Add a Node-based smoke test that calls `buildPatientAlertViewModel` and `shouldOpenPatientAlertModal`.
- [x] Run `node "AI HEALTH AGENT/test/patient_alert_ui_test.js"` and verify it fails before implementation because the helpers are missing.
- [x] Add the smallest helper implementation in `script_2.js`.
- [x] Re-run the test and verify it passes.

### Task 2: Add Web Alert Card And Modal

**Files:**
- Modify: `AI HEALTH AGENT/web/healthApp.html`
- Modify: `AI HEALTH AGENT/web/script_2.js`
- Modify: `AI HEALTH AGENT/web/style_2.css`

- [x] Add patient alert card and modal markup after the current alert banner.
- [x] Render status, hospital, heart rate, created time, and safety copy from the alert helper.
- [x] Open the modal once per fresh alert id during the current browser session.
- [x] Run `node --check "AI HEALTH AGENT/web/script_2.js"` and the alert UI test.

### Task 3: Improve Android Alert Screens

**Files:**
- Modify: `AndroidClient/app/src/main/java/za/ac/tut/healthmonitor/mobile/ui/AppScreen.kt`

- [x] Upgrade `EmergencyNotificationCard` into a richer screenshot-ready card.
- [x] Show a full-screen Compose `AlertDialog` whenever `activeAlert` first appears.
- [x] Keep the current demo-dispatch disclaimer visible.
- [x] Run the Android compile/build check available in this workspace.

### Task 4: Capture Real Screenshots

**Files:**
- No source files.

- [ ] Use the deployed app at `https://ai-health-helper.onrender.com` for web screenshots after the changes are deployed.
- [ ] Use the Android app/emulator for the dashboard alert card and full-screen alert screenshots.
- [ ] Capture hospital portal evidence showing the same alert in the hospital queue.
- [ ] Use the screenshots in the 10-slide pitch deck, 2-minute demo video, one-page summary, and clinic pilot proposal.
