-- Supabase public schema hardening.
-- The Java backend should access data through server-side database credentials.
-- Do not add broad anon/authenticated policies unless the app is redesigned to use Supabase Auth.

ALTER TABLE public.devices ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.health_sync_sections ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.patient_context_settings ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON TABLE public.devices FROM anon, authenticated;
REVOKE ALL ON TABLE public.health_sync_sections FROM anon, authenticated;
REVOKE ALL ON TABLE public.patient_context_settings FROM anon, authenticated;

ALTER VIEW IF EXISTS public.emergency_patient_dashboard SET (security_invoker = true);
ALTER VIEW IF EXISTS public.patient_doctor_summaries SET (security_invoker = true);
