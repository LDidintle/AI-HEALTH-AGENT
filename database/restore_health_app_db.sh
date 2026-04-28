#!/usr/bin/env bash
set -euo pipefail

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-healthguizer}"
BACKUP_FILE="${1:-}"

if [[ -z "$BACKUP_FILE" || ! -f "$BACKUP_FILE" ]]; then
  echo "Usage: DB_PASSWORD='your-password' ./database/restore_health_app_db.sh path/to/backup.sql"
  exit 1
fi

if [[ -z "${DB_PASSWORD:-}" ]]; then
  echo "Set DB_PASSWORD before running. Example:"
  echo "DB_PASSWORD='your-password' ./database/restore_health_app_db.sh $BACKUP_FILE"
  exit 1
fi

mysql \
  --host="$DB_HOST" \
  --port="$DB_PORT" \
  --user="$DB_USER" \
  --password="$DB_PASSWORD" \
  < "$BACKUP_FILE"

echo "Restore completed from: $BACKUP_FILE"
