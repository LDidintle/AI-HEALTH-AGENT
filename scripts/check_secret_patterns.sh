#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

cd "$ROOT_DIR"

if git grep --untracked --exclude-standard -I -n -E 'sk-[A-Za-z0-9]{20,}|AKIA[0-9A-Z]{16}|AIza[0-9A-Za-z_-]{35}' -- .; then
  echo "Found high-confidence secret pattern." >&2
  exit 1
fi

echo "Secret pattern scan passed."
