# Password Reset OTP Demo

The forgot-password flow is available from the patient login page.

## Demo Flow

1. Open `user_sign.html`.
2. Click `Forgot Password?`.
3. Enter the patient email address.
4. Click `Send OTP`.
5. If SMTP is configured, check the email inbox for the OTP.
6. If SMTP is not configured, the page shows `Demo OTP` so the flow can still be demonstrated locally.
7. Enter the OTP.
8. Enter a strong new password:
   - at least 8 characters
   - at least 2 numbers
   - at least 1 uppercase letter
   - at least 1 special character
9. Submit `Change Password`.
10. Go back to login and sign in with the new password.

## SMTP Environment Variables

Configure these to send real OTP emails:

```text
SMARTHEALTH_SMTP_HOST=smtp.gmail.com
SMARTHEALTH_SMTP_PORT=587
SMARTHEALTH_SMTP_USER=your-email@example.com
SMARTHEALTH_SMTP_PASSWORD=your-app-password
SMARTHEALTH_SMTP_FROM=your-email@example.com
SMARTHEALTH_SMTP_STARTTLS=true
SMARTHEALTH_SHOW_RESET_OTP=false
```

For local demos without SMTP, leave SMTP unset and keep `SMARTHEALTH_SHOW_RESET_OTP=true` or unset.

## Database Migration

Run the matching migration before demoing the feature on a fresh database:

- PostgreSQL/Supabase: `database/password_reset_migration_postgresql.sql`
- MariaDB: `database/password_reset_migration_mariadb.sql`

The app also attempts to create the OTP table automatically if it is missing, but running the migration is cleaner for marking.
