# Supabase Setup

This Java web app talks to Supabase through the PostgreSQL JDBC driver. Keep the database credentials out of source control and configure them as environment variables on the server that runs GlassFish/Tomcat.

## 1. Create the tables

Open your Supabase project, go to the SQL Editor, and run:

```text
database/supabase_schema.sql
```

## 2. Configure the app server

Set these environment variables before starting the Java server:

```bash
export SMARTHEALTH_DB_URL='jdbc:postgresql://YOUR-SUPABASE-HOST:5432/postgres?sslmode=require'
export SMARTHEALTH_DB_USER='postgres'
export SMARTHEALTH_DB_PASSWORD='your-supabase-db-password'
export SMARTHEALTH_STAFF_USER='your-staff-username'
export SMARTHEALTH_STAFF_PASSWORD='your-staff-password'
```

You can also paste Supabase's full `postgres://...` or `postgresql://...` connection string into `SMARTHEALTH_DB_URL`; the app normalizes it to the JDBC format at startup. If the URL already includes the username and password, you do not need to set `SMARTHEALTH_DB_USER` or `SMARTHEALTH_DB_PASSWORD`.

For most local networks, use Supabase's Session pooler connection string because it supports IPv4. Supabase documents this as the recommended option for persistent backend clients when direct IPv6 is not available. Avoid Transaction pooler mode for this servlet app because transaction pooling can break JDBC prepared statements.

## 3. Verify the connection

After deployment, test a simple page or servlet that reads users. If the server logs say `SMARTHEALTH_DB_URL or SUPABASE_DB_URL must be configured`, the environment variables are not visible to the app server process.

## Notes

- Do not commit `.env` files or copied Supabase passwords.
- The bundled PostgreSQL JDBC driver is `web/WEB-INF/lib/postgresql-42.2.27.jar`, which is compatible with this Java 8/GlassFish setup.
- Existing MariaDB support is still available if `SMARTHEALTH_DB_URL` starts with `jdbc:mariadb://`, but new Supabase deployments should use PostgreSQL.
- This local GlassFish 5.1 domain uses legacy `java.endorsed.dirs` options that break TLS with Supabase on the installed Java 8 runtime. The local `.env.local` uses the Supabase pooler with `sslmode=disable` only as a development workaround. For production, run on a newer server/runtime that supports Supabase TLS and use `sslmode=require`.
