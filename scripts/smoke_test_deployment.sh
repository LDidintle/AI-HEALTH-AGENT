#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-}"
EMAIL="${SMARTHEALTH_SMOKE_EMAIL:-}"
PASSWORD="${SMARTHEALTH_SMOKE_PASSWORD:-}"

if [ -z "$BASE_URL" ]; then
  echo "Set BASE_URL, for example: BASE_URL=https://your-render-service.onrender.com $0" >&2
  exit 2
fi

BASE_URL="${BASE_URL%/}"
COOKIE_JAR="$(mktemp)"
cleanup() {
  rm -f "$COOKIE_JAR"
}
trap cleanup EXIT

require_status() {
  local label="$1"
  local expected="$2"
  local url="$3"
  local status
  status="$(curl -sS -o /tmp/smarthealth_smoke_body.txt -w "%{http_code}" "$url")"
  if [ "$status" != "$expected" ]; then
    echo "FAIL $label: expected HTTP $expected, got $status" >&2
    cat /tmp/smarthealth_smoke_body.txt >&2 || true
    exit 1
  fi
  echo "PASS $label ($status)"
}

require_json_success() {
  local label="$1"
  local body="$2"
  if ! printf "%s" "$body" | grep -q '"success"[[:space:]]*:[[:space:]]*true'; then
    echo "FAIL $label: expected JSON success=true" >&2
    printf "%s\n" "$body" >&2
    exit 1
  fi
  echo "PASS $label"
}

require_status "landing page" "200" "$BASE_URL/"
require_status "health endpoint" "200" "$BASE_URL/health"

unauth_status="$(curl -sS -o /tmp/smarthealth_smoke_body.txt -w "%{http_code}" "$BASE_URL/api/mobile/health-sync")"
case "$unauth_status" in
  401|405)
    echo "PASS unauthenticated mobile endpoint rejects ($unauth_status)"
    ;;
  *)
    echo "FAIL unauthenticated mobile endpoint should reject, got HTTP $unauth_status" >&2
    cat /tmp/smarthealth_smoke_body.txt >&2 || true
    exit 1
    ;;
esac

if [ -n "$EMAIL" ] && [ -n "$PASSWORD" ]; then
  login_body="$(curl -sS -c "$COOKIE_JAR" \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    --data-urlencode "email=$EMAIL" \
    --data-urlencode "password=$PASSWORD" \
    "$BASE_URL/api/mobile/login")"
  require_json_success "patient mobile login" "$login_body"

  me_body="$(curl -sS -b "$COOKIE_JAR" "$BASE_URL/api/mobile/me")"
  require_json_success "mobile me endpoint" "$me_body"

  readings_body="$(curl -sS -b "$COOKIE_JAR" "$BASE_URL/api/mobile/health-sync")"
  require_json_success "latest readings endpoint" "$readings_body"

  capabilities_body="$(curl -sS -b "$COOKIE_JAR" "$BASE_URL/api/mobile/device-capabilities")"
  require_json_success "device capabilities endpoint" "$capabilities_body"
else
  echo "SKIP authenticated checks: set SMARTHEALTH_SMOKE_EMAIL and SMARTHEALTH_SMOKE_PASSWORD."
fi

echo "SmartHealth deployment smoke checks completed."
