package za.ac.tut.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.util.JsonUtil;

public class AIChatServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AIChatServlet.class.getName());
    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final int MAX_CONTEXT_LENGTH = 4000;
    private static final int LLM_TIMEOUT_SECONDS = 30;
    private static final String ENDPOINT = config("SMARTHEALTH_LLM_ENDPOINT", "https://api.openai.com/v1/responses");
    private static final String MODEL = config("SMARTHEALTH_LLM_MODEL", "gpt-5");
    private static final boolean WEB_SEARCH_ENABLED = Boolean.parseBoolean(config("SMARTHEALTH_AGENT_WEB_SEARCH", "false"));

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        String message = limit(valueOrDefault(request.getParameter("message"), ""), MAX_MESSAGE_LENGTH);
        String vitals = limit(valueOrDefault(request.getParameter("vitals"), "{}"), MAX_CONTEXT_LENGTH);
        String history = limit(valueOrDefault(request.getParameter("history"), ""), MAX_CONTEXT_LENGTH);
        String apiKey = openAiApiKey();

        if (message.isEmpty()) {
            writeReply(response, fallbackReply(message, history, vitals), "fallback_empty_message");
            return;
        }

        if (apiKey.isEmpty()) {
            writeReply(response, fallbackReply(message, history, vitals), "fallback_missing_key");
            return;
        }

        try {
            writeReply(response, requestLlmReply(message, vitals, history, apiKey), WEB_SEARCH_ENABLED ? "llm_agent" : "llm");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "SmartHealth AI request failed: {0}", e.getMessage());
            writeReply(response, fallbackReply(message, history, vitals), "fallback");
        }
    }

    private String requestLlmReply(String message, String vitals, String history, String apiKey) throws IOException {
        String body = buildRequestBody(message, vitals, history);
        try {
            return requestLlmReplyWithHttp(body, apiKey);
        } catch (LinkageError e) {
            LOGGER.log(Level.WARNING, "Java HTTPS transport failed; retrying with curl transport: {0}", e.toString());
            return requestLlmReplyWithCurl(body, apiKey);
        }
    }

    private String buildRequestBody(String message, String vitals, String history) {
        String instructions = "You are SmartHealth Agent for a student health-monitoring app. "
                + "You are a dynamic assistant that can reason over the conversation, current displayed vitals, "
                + "and reputable web sources when web search is available. Give short, clear wellness guidance only. "
                + "Use plain text without Markdown formatting. "
                + "Do not diagnose disease, prescribe treatment, or replace doctor/staff. For emergency warning signs "
                + "such as chest pain, confusion, fainting, severe weakness, shortness of breath, stroke symptoms, "
                + "or very abnormal vitals, tell the user to call emergency services or doctor/staff now. "
                + "When you use web information, keep citations visible in the answer.";
        String input = "Conversation so far:\n" + history + "\n\nUser question: " + message + "\nCurrent displayed vitals JSON: " + vitals;

        return "{"
                + "\"model\":" + JsonUtil.quote(MODEL) + ","
                + "\"instructions\":" + JsonUtil.quote(instructions) + ","
                + "\"input\":" + JsonUtil.quote(input) + ","
                + "\"reasoning\":{\"effort\":\"low\"},"
                + agentToolsJson()
                + "\"max_output_tokens\":220"
                + "}";
    }

    private String requestLlmReplyWithHttp(String body, String apiKey) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(ENDPOINT).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(12000);
        conn.setReadTimeout(20000);
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream out = conn.getOutputStream()) {
            out.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int statusCode = conn.getResponseCode();
        InputStream stream = statusCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
        String json = readAll(stream);
        if (statusCode >= 400) {
            throw new IOException("OpenAI API returned HTTP " + statusCode + ".");
        }

        String content = extractResponseText(json);
        if (content == null || content.trim().isEmpty()) {
            throw new IOException("LLM response did not include content.");
        }
        return content.trim();
    }

    private String requestLlmReplyWithCurl(String body, String apiKey) throws IOException {
        Path bodyFile = Files.createTempFile("smarthealth-openai-", ".json");
        try {
            Files.write(bodyFile, body.getBytes(StandardCharsets.UTF_8));

            ProcessBuilder builder = new ProcessBuilder(
                    "/bin/sh",
                    "-c",
                    "/usr/bin/curl --silent --show-error --max-time " + LLM_TIMEOUT_SECONDS
                            + " --write-out '\\n__HTTP_STATUS__:%{http_code}'"
                            + " -X POST"
                            + " -H \"$OPENAI_AUTH_HEADER\""
                            + " -H 'Content-Type: application/json'"
                            + " --data-binary @\"$OPENAI_BODY_FILE\""
                            + " \"$OPENAI_ENDPOINT\""
            );
            builder.environment().put("OPENAI_AUTH_HEADER", "Authorization: Bearer " + apiKey);
            builder.environment().put("OPENAI_BODY_FILE", bodyFile.toAbsolutePath().toString());
            builder.environment().put("OPENAI_ENDPOINT", ENDPOINT);
            builder.redirectErrorStream(true);

            Process process = builder.start();

            String output = readAll(process.getInputStream());
            boolean finished;
            try {
                finished = process.waitFor(LLM_TIMEOUT_SECONDS + 5L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("OpenAI curl request was interrupted.", e);
            }

            if (!finished) {
                process.destroyForcibly();
                throw new IOException("OpenAI curl request timed out.");
            }

            String statusMarkerText = "__HTTP_STATUS__:";
            int statusMarker = output.lastIndexOf(statusMarkerText);
            if (statusMarker < 0) {
                throw new IOException("OpenAI curl request did not include an HTTP status. Output: " + safeSnippet(output, apiKey));
            }

            String json = output.substring(0, statusMarker);
            int statusCode = parseStatusCode(output.substring(statusMarker + statusMarkerText.length()));
            if (statusCode >= 400 || statusCode < 200) {
                throw new IOException("OpenAI API returned HTTP " + statusCode + ".");
            }

            String content = extractResponseText(json);
            if (content == null || content.trim().isEmpty()) {
                throw new IOException("LLM response did not include content.");
            }
            return content.trim();
        } finally {
            Files.deleteIfExists(bodyFile);
        }
    }

    private static int parseStatusCode(String statusText) throws IOException {
        try {
            return Integer.parseInt(statusText.trim());
        } catch (NumberFormatException e) {
            throw new IOException("OpenAI curl request returned an invalid HTTP status.", e);
        }
    }

    private static String safeSnippet(String value, String apiKey) {
        if (value == null || value.trim().isEmpty()) return "<empty>";

        String safe = value.replace(apiKey, "[REDACTED_API_KEY]");
        int authStart = safe.indexOf("Authorization: Bearer ");
        if (authStart >= 0) {
            int authEnd = safe.indexOf('\n', authStart);
            if (authEnd < 0) authEnd = safe.length();
            safe = safe.substring(0, authStart) + "Authorization: Bearer [REDACTED]" + safe.substring(authEnd);
        }
        safe = safe.replace('\n', ' ').replace('\r', ' ').trim();
        return safe.length() <= 300 ? safe : safe.substring(0, 300);
    }

    private static String fallbackReply(String message, String history, String vitalsJson) {
        String combined = ((history == null ? "" : history) + "\n" + (message == null ? "" : message));
        String lower = combined.toLowerCase();
        VitalSnapshot vitals = VitalSnapshot.fromJson(vitalsJson);
        String vitalSummary = vitals.summary();

        if (hasAny(lower, "dying", "can't breathe", "cannot breathe", "short of breath", "chest pain", "crushing pain", "stroke", "seizure", "faint", "fainted", "confused", "confusion", "blue lips", "severe bleeding", "unconscious")) {
            return "This may be an emergency. Call emergency services or doctor/staff now, especially if you have chest pain, trouble breathing, fainting, confusion, severe weakness, or feel like you may die. " + vitalSummary + " Sit or lie down safely while waiting for help, and do not drive yourself.";
        }

        if (vitals.hasCriticalReading()) {
            return "Your displayed readings include a warning sign: " + vitals.alertSummary() + " Please contact doctor/staff now, and call emergency services if you also have chest pain, shortness of breath, fainting, confusion, or severe weakness.";
        }

        if (lower.contains("help") && hasAny(lower, "urgent", "scared", "worried", "panic", "bad", "sick")) {
            return "I hear that you are worried. " + vitalSummary + " Tell doctor/staff what you are feeling and when it started. If symptoms feel severe or suddenly worse, treat it as urgent and call emergency services.";
        }

        if (lower.contains("heart") || lower.contains("pulse") || lower.contains("bpm")) {
            return vitals.heartRateAdvice();
        }

        if (lower.contains("blood") || lower.contains("pressure")) {
            return vitals.bloodPressureAdvice();
        }

        if (lower.contains("temp") || lower.contains("fever") || lower.contains("hot") || lower.contains("cold")) {
            return vitals.temperatureAdvice();
        }

        if (lower.contains("watch") || lower.contains("sync")) {
            return "Pair the watch with Samsung Health, allow Health Connect access, then sync from the Android app. If readings still do not update, reopen the app and check Health Connect permissions.";
        }

        return "Based on the displayed readings, " + vitalSummary + " I can help you understand heart rate, blood pressure, temperature, symptoms, or when to contact doctor/staff. This is wellness guidance, not a diagnosis.";
    }

    private static boolean hasAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) return true;
        }
        return false;
    }

    private static final class VitalSnapshot {
        private static final Pattern NUMBER_FIELD = Pattern.compile("\"([A-Za-z]+)\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");

        private final Double heartRate;
        private final Double temperature;
        private final Double systolic;
        private final Double diastolic;

        private VitalSnapshot(Double heartRate, Double temperature, Double systolic, Double diastolic) {
            this.heartRate = heartRate;
            this.temperature = temperature;
            this.systolic = systolic;
            this.diastolic = diastolic;
        }

        private static VitalSnapshot fromJson(String json) {
            Double heartRate = null;
            Double temperature = null;
            Double systolic = null;
            Double diastolic = null;

            if (json != null) {
                Matcher matcher = NUMBER_FIELD.matcher(json);
                while (matcher.find()) {
                    String key = matcher.group(1);
                    Double value = Double.valueOf(matcher.group(2));
                    if ("heartRate".equals(key)) heartRate = value;
                    if ("temperature".equals(key)) temperature = value;
                    if ("systolic".equals(key)) systolic = value;
                    if ("diastolic".equals(key)) diastolic = value;
                }
            }

            return new VitalSnapshot(heartRate, temperature, systolic, diastolic);
        }

        private boolean hasCriticalReading() {
            return (heartRate != null && (heartRate < 45 || heartRate > 130))
                    || (temperature != null && (temperature < 35.0 || temperature >= 39.0))
                    || (systolic != null && diastolic != null && (systolic >= 180 || diastolic >= 120 || systolic < 80 || diastolic < 50));
        }

        private String alertSummary() {
            if (heartRate != null && heartRate < 45) return "heart rate is very low at " + rounded(heartRate) + " BPM.";
            if (heartRate != null && heartRate > 130) return "heart rate is very high at " + rounded(heartRate) + " BPM.";
            if (temperature != null && temperature >= 39.0) return "temperature is high at " + oneDecimal(temperature) + " C.";
            if (temperature != null && temperature < 35.0) return "temperature is low at " + oneDecimal(temperature) + " C.";
            if (systolic != null && diastolic != null && (systolic >= 180 || diastolic >= 120)) return "blood pressure is very high at " + rounded(systolic) + "/" + rounded(diastolic) + " mmHg.";
            if (systolic != null && diastolic != null && (systolic < 80 || diastolic < 50)) return "blood pressure is very low at " + rounded(systolic) + "/" + rounded(diastolic) + " mmHg.";
            return "one or more readings may need attention.";
        }

        private String summary() {
            StringBuilder builder = new StringBuilder();
            if (heartRate != null) builder.append("heart rate ").append(rounded(heartRate)).append(" BPM");
            if (systolic != null && diastolic != null) appendWithComma(builder, "blood pressure " + rounded(systolic) + "/" + rounded(diastolic) + " mmHg");
            if (temperature != null) appendWithComma(builder, "temperature " + oneDecimal(temperature) + " C");
            if (builder.length() == 0) return "I do not have synced section readings yet.";
            return "Your current displayed readings are " + builder + ".";
        }

        private String heartRateAdvice() {
            if (heartRate == null) return "I do not have a heart-rate reading yet. Sync the latest phone health section first, then ask again with symptoms if you feel unwell.";
            if (heartRate < 50) return "Your heart rate is " + rounded(heartRate) + " BPM, which is low for many adults. If you feel dizzy, faint, weak, confused, or short of breath, contact doctor/staff urgently.";
            if (heartRate > 120) return "Your heart rate is " + rounded(heartRate) + " BPM, which is high for a resting reading. Sit down, rest, recheck it, and contact doctor/staff if it stays high or you feel chest pain, faintness, or shortness of breath.";
            return "Your heart rate is " + rounded(heartRate) + " BPM, which is not severely abnormal for many adults. Trends and symptoms still matter, so tell doctor/staff if you feel unwell.";
        }

        private String bloodPressureAdvice() {
            if (systolic == null || diastolic == null) return "I do not have a blood-pressure reading yet. Recheck after resting and sync the latest reading.";
            if (systolic >= 140 || diastolic >= 90) return "Your blood pressure is " + rounded(systolic) + "/" + rounded(diastolic) + " mmHg, which is above the usual target range. Recheck after five minutes of rest and share it with doctor/staff if it remains high.";
            if (systolic < 90 || diastolic < 60) return "Your blood pressure is " + rounded(systolic) + "/" + rounded(diastolic) + " mmHg, which is low. Seek help if you feel dizzy, faint, confused, weak, or short of breath.";
            return "Your blood pressure is " + rounded(systolic) + "/" + rounded(diastolic) + " mmHg, which is not severely abnormal from this single reading. Keep watching trends and symptoms.";
        }

        private String temperatureAdvice() {
            if (temperature == null) return "I do not have a temperature reading yet. Sync the latest reading and watch for fever, chills, confusion, or worsening symptoms.";
            if (temperature >= 38.0) return "Your temperature is " + oneDecimal(temperature) + " C, which may be a fever. Rest, hydrate, monitor symptoms, and contact doctor/staff if it persists or you feel very unwell.";
            if (temperature < 35.5) return "Your temperature is " + oneDecimal(temperature) + " C, which is low. Warm up safely and seek help if you feel confused, very cold, weak, or drowsy.";
            return "Your temperature is " + oneDecimal(temperature) + " C, which is within a common normal range. Keep monitoring if you have symptoms.";
        }

        private static void appendWithComma(StringBuilder builder, String text) {
            if (builder.length() > 0) builder.append(", ");
            builder.append(text);
        }

        private static String rounded(Double value) {
            return String.valueOf(Math.round(value));
        }

        private static String oneDecimal(Double value) {
            return String.format(java.util.Locale.US, "%.1f", value);
        }
    }

    private static String agentToolsJson() {
        if (!WEB_SEARCH_ENABLED) return "";

        return "\"tools\":[{"
                + "\"type\":\"web_search\","
                + "\"external_web_access\":true,"
                + "\"filters\":{\"allowed_domains\":["
                + "\"www.who.int\","
                + "\"www.cdc.gov\","
                + "\"www.nhs.uk\","
                + "\"www.mayoclinic.org\","
                + "\"medlineplus.gov\","
                + "\"www.health.gov.za\","
                + "\"www.nicd.ac.za\""
                + "]}"
                + "}],"
                + "\"tool_choice\":\"auto\","
                + "\"include\":[\"web_search_call.action.sources\"],";
    }

    private static String extractResponseText(String json) {
        String outputText = extractStringField(json, "output_text");
        if (outputText != null) return outputText;

        String text = extractStringField(json, "text");
        if (text != null) return text;

        return extractStringField(json, "content");
    }

    private static String extractStringField(String json, String fieldName) {
        String marker = "\"" + fieldName + "\"";
        int start = json.indexOf(marker);
        if (start < 0) return null;

        int colon = json.indexOf(':', start + marker.length());
        if (colon < 0) return null;

        int valueStart = colon + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        if (valueStart >= json.length() || json.charAt(valueStart) != '"') return null;

        StringBuilder result = new StringBuilder();
        boolean escaped = false;
        for (int i = valueStart + 1; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (escaped) {
                if (ch == 'u' && i + 4 < json.length()) {
                    String hex = json.substring(i + 1, i + 5);
                    result.append((char) Integer.parseInt(hex, 16));
                    i += 4;
                } else {
                    result.append(unescape(ch));
                }
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else if (ch == '"') {
                return result.toString();
            } else {
                result.append(ch);
            }
        }
        return null;
    }

    private static char unescape(char ch) {
        if (ch == 'n') return '\n';
        if (ch == 'r') return '\r';
        if (ch == 't') return '\t';
        return ch;
    }

    private static String readAll(InputStream stream) throws IOException {
        if (stream == null) return "";
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) builder.append(line);
        }
        return builder.toString();
    }

    private static String valueOrDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private static String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) return value;
        return value.substring(0, maxLength);
    }

    private static String config(String name, String fallback) {
        String property = System.getProperty(name);
        if (property != null && !property.trim().isEmpty()) return property.trim();

        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private static String openAiApiKey() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey != null && !apiKey.trim().isEmpty()) return apiKey.trim();
        return config("SMARTHEALTH_LLM_API_KEY", "");
    }

    private static void writeReply(HttpServletResponse response, String reply, String source) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.write("{\"success\":true,\"source\":" + JsonUtil.quote(source) + ",\"model\":" + JsonUtil.quote(MODEL) + ",\"reply\":" + JsonUtil.quote(reply) + "}");
        }
    }
}
