# SmartHealth AI Documentation

## 1. Purpose of the AI Feature

SmartHealth uses AI-assisted functionality to support a health-monitoring workflow for patients, staff, and hospital users. The AI features are designed for educational, non-diagnostic wellness guidance. They do not replace a doctor, clinic, hospital, emergency service, or qualified healthcare professional.

The system uses two intelligent components:

1. A rule-based screening engine that analyses synced vital signs and produces a risk level, score, reasons, and recommended action.
2. An optional OpenAI-powered chat assistant that answers patient wellness questions using the user's current displayed vitals and conversation context.

These two components support the business goal of helping users and staff notice abnormal health patterns earlier, understand the meaning of displayed readings, and decide when to seek human medical support.

## 2. AI Model Logic and Selection

### 2.1 Rule-Based Screening Model

The backend class `HealthRiskPredictionService` implements a deterministic screening model called:

```text
RULE_BASED_SCREENING_V1
```

This model uses recent synced health sections from the database and evaluates:

- heart rate
- temperature
- blood pressure
- repeated abnormal sections
- whether Samsung watch temperature should be treated as sleep-temperature trend data only

The output includes:

- `riskLevel`: `LOW`, `MEDIUM`, `HIGH`, or `URGENT`
- `score`: numeric risk score from `0` to `100`
- `summary`: short screening summary
- `reasons`: factors that influenced the score
- `recommendedAction`: suggested next step
- `dataQuality`: whether the result has enough vitals or limited data
- `diagnosticDisclaimer`: a clear statement that the result is not a diagnosis

This model was selected because the project handles health data, so predictable and explainable logic is safer than a hidden or unclear prediction. It also lets staff and lecturers see exactly why a risk level was produced.

### 2.2 OpenAI Chat Assistant

The backend servlet `AIChatServlet` integrates with the OpenAI Responses API when `OPENAI_API_KEY` is configured. The model name is read from:

```text
SMARTHEALTH_LLM_MODEL
```

If no model is configured, the servlet defaults to the configured value in code. The chat assistant receives:

- the patient's chat message
- recent conversation history
- current displayed vitals from the web or mobile UI

The assistant is instructed to provide short wellness guidance only. It is specifically instructed not to diagnose disease, prescribe treatment, or replace doctor/staff.

The OpenAI assistant was selected because it provides natural-language explanations for users who may not understand medical-style values such as blood pressure, pulse, or temperature trend data. This improves usability while keeping the core health classification deterministic and explainable.

## 3. Business Value

The AI features support the SmartHealth business case in the following ways:

- Patients receive understandable wellness guidance instead of only raw numbers.
- Staff can see a screening note and risk level that summarizes recent vitals.
- Hospital users can review alert context and patient details faster.
- The Android and web dashboards can explain missing or limited data instead of silently failing.
- The system can guide users to contact doctor/staff when readings or symptoms are concerning.

This adds value because the system does not only store patient data; it interprets recent readings and presents them in a form that supports action.

## 4. 3-Tier AI Integration

SmartHealth follows a 3-tier architecture:

```text
Presentation Tier
Patient web dashboard, admin/staff pages, hospital portal, Android app

Application Tier
Java servlets, validation, session handling, AI prompt construction, prediction logic

Data Tier
Supabase PostgreSQL or MariaDB health database
```

### 4.1 Rule-Based Prediction Flow

```text
Android app or patient dashboard
        ->
Java backend requests latest readings
        ->
HealthRiskPredictionService reads health_sync_sections
        ->
Screening model calculates risk level and reasons
        ->
Backend returns JSON prediction
        ->
UI displays wellness suggestions and screening note
```

The prediction is returned by backend endpoints such as:

- `ReadingServlet.do`
- `api/mobile/health-sync`

### 4.2 OpenAI Chat Flow

```text
Patient enters a chat message
        ->
Frontend sends message, history, and vitals to AIChatServlet
        ->
AIChatServlet validates and limits input size
        ->
AIChatServlet builds a safety-focused prompt
        ->
OpenAI Responses API is called when OPENAI_API_KEY is configured
        ->
Short wellness guidance is returned to the UI
```

If the OpenAI API key is not configured or the API request fails, the servlet returns fallback rule-based guidance instead of crashing.

## 5. Technical Implementation

### 5.1 Main AI Files

| File | Responsibility |
|---|---|
| `AI HEALTH AGENT/src/java/za/ac/tut/web/AIChatServlet.java` | Handles AI chat requests and calls the OpenAI Responses API. |
| `AI HEALTH AGENT/src/java/za/ac/tut/util/HealthRiskPredictionService.java` | Calculates rule-based risk score and screening explanation. |
| `AI HEALTH AGENT/src/java/za/ac/tut/util/WatchTemperaturePolicy.java` | Handles Samsung watch temperature caveats. |
| `AndroidClient/app/src/main/java/za/ac/tut/healthmonitor/mobile/insights/HealthInsightEngine.kt` | Provides Android-side wellness insight text. |
| `AI HEALTH AGENT/web/script_2.js` | Sends chat/vitals from the patient dashboard and displays AI responses. |

### 5.2 Environment Variables

The AI chat feature uses environment variables instead of hardcoded secrets:

```text
OPENAI_API_KEY
SMARTHEALTH_LLM_MODEL
SMARTHEALTH_LLM_ENDPOINT
SMARTHEALTH_AGENT_WEB_SEARCH
```

The API key is never stored in source code. It must be configured on the deployment platform, such as Render, or in a local `.env` file that is not committed.

### 5.3 Input Limits

`AIChatServlet` limits input sizes before sending data to the AI service:

```text
MAX_MESSAGE_LENGTH = 1000
MAX_CONTEXT_LENGTH = 4000
```

This reduces the risk of very large prompts, accidental sensitive-data overexposure, and abuse.

### 5.4 Rate Limiting

The AI chat endpoint uses `RateLimitService`:

```text
30 requests per 15 minutes per client key
```

This protects the system from excessive API usage and helps control cost.

## 6. User Experience

The AI output is shown in user-facing areas:

- patient web dashboard chat modal
- Android assistant/chat experience
- wellness suggestions
- screening notes for synced readings
- hospital/staff patient summaries

The UI avoids presenting AI output as a diagnosis. The language is framed as:

- wellness guidance
- screening pattern
- recommended action
- contact doctor/staff when worried
- call emergency services for severe symptoms

This makes the AI useful while reducing the risk of users treating it as professional medical advice.

## 7. Ethics, Privacy, and Hallucination Handling

### 7.1 Medical Safety

The system includes a medical disclaimer in the project documentation and AI prompt. The AI is instructed:

- not to diagnose disease
- not to prescribe treatment
- not to replace doctor/staff
- to recommend emergency help for serious symptoms
- to treat Samsung watch temperature as trend data when applicable

### 7.2 Fallback Behaviour

If the OpenAI API is unavailable, the system does not fail completely. `AIChatServlet` returns fallback guidance based on:

- the user's message
- displayed vitals
- emergency keywords
- heart rate, blood pressure, and temperature checks

This prevents the user from seeing a broken AI feature during normal use.

### 7.3 Hallucination Reduction

The project reduces hallucination risk by:

- using deterministic rule-based screening for core risk scoring
- keeping OpenAI chat responses short
- giving the model a strict wellness-only instruction
- passing current displayed vitals as context
- including disclaimers in model output and rule-based prediction output
- using fallback responses for known health topics

The system does not allow the AI to directly change medical records, update readings, or make database decisions.

### 7.4 Data Privacy

The AI request is limited to the message, conversation history, and displayed vital context. The API key is stored as an environment variable. The application should avoid sending unnecessary personal information such as ID numbers, addresses, or full patient profile details to the AI model.

For production use, the project should add a formal privacy policy, user consent tracking, and stricter audit logging before sending any sensitive health data to an external AI provider.

## 8. Example AI Output

Example rule-based prediction output:

```json
{
  "modelType": "RULE_BASED_SCREENING_V1",
  "riskLevel": "LOW",
  "score": 10,
  "summary": "Low-risk screening pattern.",
  "reasons": [
    "Samsung watch temperature is treated as a sleep-temperature trend and is not scored as core body temperature.",
    "Blood pressure is missing."
  ],
  "recommendedAction": "Keep monitoring trends. This score does not diagnose or rule out illness.",
  "dataQuality": "LIMITED_DATA",
  "diagnosticDisclaimer": "This is a rule-based screening score, not a diagnosis and not a trained machine-learning model."
}
```

Example chat response:

```text
Your displayed readings look stable right now, but keep monitoring them. If you develop chest pain, trouble breathing, fainting, confusion, or severe weakness, contact emergency services or doctor/staff immediately. This is wellness guidance, not a diagnosis.
```

## 9. Limitations

The AI feature has important limitations:

- The rule-based screening model is not a trained medical ML model.
- The system depends on available watch/mobile data.
- Samsung watch temperature may be sleep-temperature trend data, not a core fever reading.
- Blood pressure availability depends on Samsung Health/watch calibration and source support.
- The OpenAI chat assistant depends on a configured API key and network access.
- The AI does not replace clinical judgement.

## 10. Future Improvements

Possible future AI improvements include:

- adding a trained model after collecting a properly labelled dataset
- adding stronger evaluation tests for AI responses
- storing AI chat logs with consent for audit and quality review
- adding clinician-approved prompt templates
- adding more detailed source citations when web search is enabled
- using structured JSON output from the AI for safer UI rendering
- adding a privacy review before sending richer patient data to the AI service

## 11. Rubric Mapping

| Rubric Requirement | SmartHealth Evidence |
|---|---|
| Model logic and selection | Rule-based `RULE_BASED_SCREENING_V1` plus optional OpenAI Responses API chat assistant. |
| Business value | Helps patients and staff understand readings, screening risk, and when to seek help. |
| Technical integration | AI flows through UI, Java servlet backend, database readings, and optional OpenAI service. |
| User experience | AI output appears as chat guidance, wellness suggestions, and screening notes. |
| Ethics | Non-diagnostic disclaimers, fallback guidance, emergency escalation wording, privacy-aware API key handling. |
| Hallucination handling | Deterministic scoring for core risk, strict prompt, short outputs, fallback logic, no AI write access to records. |
