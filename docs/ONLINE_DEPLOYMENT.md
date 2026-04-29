# Online Deployment Guide

## Current local status

The app is a Java 8 / Servlet / JSP web application that builds a WAR:

```sh
./scripts/deploy_local_glassfish.sh .env
```

The online deployment path uses the root `Dockerfile`, which builds the servlet app and runs it on Tomcat 9. In the container the app is deployed as `ROOT.war`, so the hosted dashboard is available at `/healthApp.html`.

Local URL:

```text
http://localhost:8080/SWP_MergedProject2/
```

## Why local login can fail

Login depends on these environment variables being visible to the GlassFish process:

```text
SMARTHEALTH_DB_URL
SMARTHEALTH_DB_USER
SMARTHEALTH_DB_PASSWORD
SMARTHEALTH_STAFF_USER
SMARTHEALTH_STAFF_PASSWORD
OPENAI_API_KEY
SMARTHEALTH_LLM_MODEL
```

If NetBeans starts GlassFish without those values, patient login can fail with server errors and staff login will always reject credentials.

## Online hosting recommendation

Use a Docker-capable Java web host and a hosted PostgreSQL database.

Good deployment shape:

```text
Browser / Android app
        |
Cloud Java web app running the WAR
        |
Supabase PostgreSQL
        |
OpenAI Responses API
```

Do not use the local MariaDB IP address online. A cloud host cannot reliably reach a private address such as `10.x.x.x`.

## Required cloud environment variables

Set these as secrets/environment variables in the hosting dashboard:

```text
OPENAI_API_KEY
SMARTHEALTH_LLM_MODEL=gpt-5.4-mini
SMARTHEALTH_AGENT_WEB_SEARCH=false

SMARTHEALTH_DB_URL=jdbc:postgresql://YOUR-SUPABASE-HOST:5432/postgres?sslmode=require
SMARTHEALTH_DB_USER=YOUR_SUPABASE_DB_USER
SMARTHEALTH_DB_PASSWORD=YOUR_SUPABASE_DB_PASSWORD

SMARTHEALTH_STAFF_USER=healthguizer
SMARTHEALTH_STAFF_PASSWORD=YOUR_STRONG_STAFF_PASSWORD
```

## Database setup

For Supabase:

1. Create a Supabase project.
2. Run `database/supabase_schema.sql` in the SQL editor.
3. Copy the Postgres connection string from Supabase.
4. Use `sslmode=require` in `SMARTHEALTH_DB_URL`.
5. Do not commit Supabase passwords or `.env` files.

## Render / Railway notes

Render supports Docker web services and lets you configure environment variables/secrets for the service.

Railway supports service variables and can deploy from a Dockerfile. If the Dockerfile is not in the repository root, configure the Dockerfile path in Railway.

## Render steps

1. Push this repository to GitHub.
2. In Render, create a new Web Service from the GitHub repo.
3. Choose Docker. Render will use the root `Dockerfile`.
4. Add the environment variables from this guide. Do not paste them into files in the repo.
5. Set the health check path to `/healthApp.html` if Render does not pick it up from `render.yaml`.
6. Deploy and open the generated Render URL.

## Local Docker smoke test

Before pushing a deployment change, run:

```sh
docker build -t smarthealth-agent .
docker run --rm --env-file .env -p 8081:8080 smarthealth-agent
```

Then check:

```text
http://localhost:8081/healthApp.html
http://localhost:8081/user_sign.html
http://localhost:8081/admin_sign.html
```

## Before going live

- Rotate any OpenAI key pasted into chat or screenshots.
- Use a new staff password for production.
- Confirm patient login, staff login, AI chat, profile loading, readings loading, and mobile API login after deployment.
- Check cloud logs immediately after the first deploy for database connection errors.
