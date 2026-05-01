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
  SMARTHEALTH_STAFF_PASSWORD \
  SMARTHEALTH_HOSPITAL_USER \
  SMARTHEALTH_HOSPITAL_PASSWORD; do
  value="${!key:-}"
  while [[ ${#value} -ge 2 ]]; do
    first="${value:0:1}"
    last="${value: -1}"
    if [[ "$first" == '"' && "$last" == '"' ]] || [[ "$first" == "'" && "$last" == "'" ]]; then
      value="${value:1:${#value}-2}"
      value="${value#"${value%%[![:space:]]*}"}"
      value="${value%"${value##*[![:space:]]}"}"
    else
      break
    fi
  done
  export "$key=$value"
done

# Render may assign a PORT value that conflicts with Tomcat's shutdown port.
# Disable the shutdown listener so external HTTP health checks always hit the web connector.
sed -i 's/<Server port="[0-9-]*" shutdown="SHUTDOWN">/<Server port="-1" shutdown="SHUTDOWN">/' "$CATALINA_HOME/conf/server.xml"
sed -i "s/<Connector port=\"[0-9]*\" protocol=\"HTTP\\/1.1\"/<Connector port=\"${PORT}\" protocol=\"HTTP\\/1.1\"/" "$CATALINA_HOME/conf/server.xml"

exec "$@"
