# Premium 4K Pitch Video Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the SmartHealth AI Health Agent proposal video as a polished 4K health-tech pitch/demo using freshly captured screenshots where possible.

**Architecture:** Keep the existing proposal folder as the artifact destination and do not change application code. Recapture high-resolution web evidence from the deployed site, reuse or recapture Android evidence if available, then generate a new premium MP4 with cinematic 4K frames, subtle transitions, sharper typography, safety positioning, and pilot-ready messaging.

**Tech Stack:** Python, Pillow, imageio-ffmpeg/FFmpeg, Playwright if available, existing deployed SmartHealth demo URL, existing proposal screenshots.

---

### Task 1: Recapture Evidence Assets

**Files:**
- Modify: `/Users/didintlemakhubedu/Documents/Repos/proposals/assets/*.png`
- Create: `/tmp/capture_smarthealth_premium_assets.mjs`

- [x] **Step 1: Capture high-resolution web screenshots**

Create a Playwright script that signs in to the deployed app with documented demo credentials, captures patient alert, hospital queue, and any available patient/staff detail screens at a high viewport size, and writes them into the proposal assets folder.

- [x] **Step 2: Preserve existing Android screenshots if emulator recapture is unavailable**

If Android emulator recapture is available within the session, replace Android assets with fresh captures. If it is not stable, keep the existing real emulator screenshots and compensate in the video layout by framing them as phone mockups without over-enlarging blurry UI text.

### Task 2: Generate Premium Video

**Files:**
- Create: `/tmp/build_smarthealth_premium_video.py`
- Create: `/Users/didintlemakhubedu/Documents/Repos/proposals/video/smarthealth-2-minute-demo-video-premium-4k.mp4`

- [x] **Step 1: Build cinematic 4K frames**

Create each slide as a 3840x2160 frame with a premium dark green/teal theme, strong hierarchy, intentional spacing, evidence cards, workflow chips, and preserved safety text.

- [x] **Step 2: Encode smooth transitions**

Use FFmpeg to encode at 3840x2160, 30 FPS, high-quality H.264 settings, with subtle crossfades and no distracting effects.

### Task 3: Verify and Package

**Files:**
- Modify: `/Users/didintlemakhubedu/Documents/Repos/proposals/README.md`

- [x] **Step 1: Verify metadata**

Confirm the premium MP4 is 3840x2160, 30 FPS, and approximately two minutes.

- [x] **Step 2: Visual QA**

Extract representative frames/contact sheet and inspect for layout, spelling, readability, and safety wording.

- [x] **Step 3: Update README**

List the premium 4K file as the recommended investor/clinic/accelerator video while preserving the earlier 4K and 720p files.
