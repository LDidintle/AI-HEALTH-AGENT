-- Supabase public schema hardening.
-- The Java backend should access data through server-side database credentials.
-- Do not add broad anon/authenticated policies unless the app is redesigned to use Supabase Auth.

ALTER TABLE public.devices ENABLE ROW LEVEL SECURITY;
