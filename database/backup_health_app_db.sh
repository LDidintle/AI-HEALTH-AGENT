#!/usr/bin/env bash
set -euo pipefail

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-health_app_db}"
DB_USER="${DB_USER:-healthguizer}"
BACKUP_DIR="${BACKUP_DIR:-database/backups}"

mkdir -p "$BACKUP_DIR"

timestamp="$(date +%Y%m%d_%H%M%S)"
output_file="$BACKUP_DIR/${DB_NAME}_backup_${timestamp}.sql"

if [[ -z "${DB_PASSWORD:-}" ]]; then
  echo "Set DB_PASSWORD before running. Example:"
  echo "DB_PASSWORD='your-password' ./database/backup_health_app_db.sh"
  exit 1
fi

mysqldump \
  --host="$DB_HOST" \
  --port="$DB_PORT" \
  --user="$DB_USER" \
  --password="$DB_PASSWORD" \
  --databases "$DB_NAME" \
  --routines \
  --events \
  --triggers \
  > "$output_file"

echo "Backup created: $output_file"
