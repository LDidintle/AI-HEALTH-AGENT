#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

cd "$ROOT_DIR"

if git grep --untracked --exclude-standard -I -n -E 'api/mobile/(alerts|health-sync|health-section-sync)\?email|email is required when no session|provide an email address|currentUserEmail' \
  -- 'AI HEALTH AGENT' 'AndroidClient/app/src/main' README.md docs; then
  echo "Found forbidden mobile email/session fallback pattern." >&2
  exit 1
fi

echo "Mobile session-auth scan passed."
