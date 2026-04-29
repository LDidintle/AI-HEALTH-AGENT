#!/usr/bin/env bash
set -euo pipefail

PORT="${PORT:-8080}"

if ! [[ "$PORT" =~ ^[0-9]+$ ]]; then
  echo "PORT must be a number." >&2
  exit 1
fi

for key in \
  OPENAI_API_KEY \
  SMARTHEALTH_LLM_MODEL \
  SMARTHEALTH_AGENT_WEB_SEARCH \
  SMARTHEALTH_DB_URL \
  SMARTHEALTH_DB_USER \
  SMARTHEALTH_DB_PASSWORD \
  SMARTHEALTH_STAFF_USER \
  SMARTHEALTH_STAFF_PASSWORD; do
  value="${!key:-}"
  if [[ "$value" == \"*\" && "$value" == *\" ]]; then
    export "$key=${value:1:${#value}-2}"
  fi
done

sed -i "s/port=\"8080\"/port=\"${PORT}\"/" "$CATALINA_HOME/conf/server.xml"

exec "$@"
