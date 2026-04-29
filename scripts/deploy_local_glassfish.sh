#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP_DIR="$ROOT_DIR/AI HEALTH AGENT"
ENV_FILE="${1:-$ROOT_DIR/.env}"
DOMAIN="${GLASSFISH_DOMAIN:-domain1}"
CONTEXT_ROOT="${SMARTHEALTH_CONTEXT_ROOT:-/SWP_MergedProject2}"
APP_NAME="${SMARTHEALTH_APP_NAME:-SWP_MergedProject2}"
GLASSFISH_HOME="${GLASSFISH_HOME:-/Users/didintlemakhubedu/NetBeansJDKs/glassfish}"
ASADMIN="$GLASSFISH_HOME/bin/asadmin"
ANT_BIN="${ANT_BIN:-/Applications/Apache NetBeans.app/Contents/Resources/netbeans/extide/ant/bin/ant}"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Missing env file: $ENV_FILE" >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

required=(
  OPENAI_API_KEY
  SMARTHEALTH_LLM_MODEL
  SMARTHEALTH_DB_URL
  SMARTHEALTH_DB_USER
  SMARTHEALTH_DB_PASSWORD
  SMARTHEALTH_STAFF_USER
  SMARTHEALTH_STAFF_PASSWORD
)

for key in "${required[@]}"; do
  if [[ -z "${!key:-}" ]]; then
    echo "Missing required setting in $ENV_FILE: $key" >&2
    exit 1
  fi
done

"$ANT_BIN" -q -f "$APP_DIR/build.xml" clean dist

launchctl remove smarthealth-glassfish 2>/dev/null || true
"$ASADMIN" stop-domain "$DOMAIN" >/dev/null 2>&1 || true

for port in 8080 4848; do
  for pid in $(lsof -nP -iTCP:"$port" -sTCP:LISTEN -t 2>/dev/null || true); do
    kill "$pid" 2>/dev/null || true
  done
done

sleep 2

export JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/temurin-8.jdk/Contents/Home}"
export AS_JAVA="${AS_JAVA:-$JAVA_HOME}"
export PATH="/usr/bin:/bin:/usr/sbin:/sbin:$JAVA_HOME/bin:$GLASSFISH_HOME/bin:$PATH"

for key in "${required[@]}" SMARTHEALTH_AGENT_WEB_SEARCH; do
  [[ -n "${!key:-}" ]] && launchctl setenv "$key" "${!key}"
done
launchctl setenv JAVA_HOME "$JAVA_HOME"
launchctl setenv AS_JAVA "$AS_JAVA"
launchctl setenv PATH "$PATH"

launchctl submit -l smarthealth-glassfish -- "$ASADMIN" start-domain --verbose "$DOMAIN"
sleep 10

"$ASADMIN" deploy --force=true --name "$APP_NAME" --contextroot "$CONTEXT_ROOT" "$APP_DIR/dist/SWP_MergedProject2.war"

echo "Deployed $APP_NAME at http://localhost:8080$CONTEXT_ROOT/"
echo "Patient login: http://localhost:8080$CONTEXT_ROOT/user_sign.html"
echo "Staff login:   http://localhost:8080$CONTEXT_ROOT/admin_sign.html"
