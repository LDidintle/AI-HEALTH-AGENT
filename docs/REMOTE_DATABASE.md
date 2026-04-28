# Remote Database Connection Notes

For new deployments, use Supabase PostgreSQL and follow `docs/SUPABASE_SETUP.md`.

The notes below document the older local MariaDB demo setup only.

## Current Status

MariaDB is running through Homebrew and listens on TCP port `3306`.

Current Mac LAN IP during testing:

```text
10.0.0.107
```

The server is reachable on that IP. A dedicated limited remote user has been created and tested:

```text
health_app_remote@10.0.0.%
```

The credential is stored locally in:

```text
database/remote_db_credentials.local.txt
```

Do not publish or submit that credential file.

## Safer Remote Access Approach

Use a dedicated remote user instead of reusing the local application user.

Recommended scope:

```sql
'health_app_remote'@'10.0.0.%'
```

Recommended privileges:

```sql
SELECT, INSERT, UPDATE, DELETE ON health_app_db.*
```

Avoid:

```sql
'user'@'%'
GRANT ALL PRIVILEGES ON *.*
```

## Setup Script

Use:

```text
database/setup_remote_user.sql
```

The script is retained as the repeatable setup reference. A live remote user has already been created for the current local demo environment.

## Test Command

After creating the remote user:

```bash
source database/remote_db_credentials.local.txt

mysql -h "$DB_HOST" -P "$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" \
  -e "SELECT COUNT(*) FROM users;"
```

## Firewall Note

The macOS firewall is enabled. Inspection showed that incoming connections for MariaDB and Java/GlassFish are currently permitted. No firewall rule was changed during this setup.

A more professional production setup would not expose MariaDB directly; it would expose HTTPS backend APIs and keep the database private.
