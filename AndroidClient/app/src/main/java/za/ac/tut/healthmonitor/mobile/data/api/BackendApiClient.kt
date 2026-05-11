package za.ac.tut.healthmonitor.mobile.data.api

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.Locale
import za.ac.tut.healthmonitor.mobile.BuildConfig
import za.ac.tut.healthmonitor.mobile.data.model.AiChatResponse
import za.ac.tut.healthmonitor.mobile.data.model.HealthSectionSyncPayload
import za.ac.tut.healthmonitor.mobile.data.model.HealthSyncPayload
import za.ac.tut.healthmonitor.mobile.data.model.LatestReadingsResponse
import za.ac.tut.healthmonitor.mobile.data.model.LoginResponse
import za.ac.tut.healthmonitor.mobile.data.model.ProfileResponse
import za.ac.tut.healthmonitor.mobile.data.model.SyncResponse

class BackendApiClient(
    private val cookieJar: SessionCookieJar = SessionCookieJar(),
    private val gson: Gson = Gson()
) {

    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .build()

    fun login(email: String, password: String): LoginResponse {
        val body = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .build()

        return post("api/mobile/login", body, LoginResponse::class.java)
    }

    fun register(
        firstName: String,
        surname: String,
        email: String,
        dob: String,
        password: String
    ): LoginResponse {
        val body = FormBody.Builder()
            .add("firstName", firstName)
            .add("surname", surname)
            .add("email", email)
            .add("dob", dob)
            .add("password", password)
            .build()

        return post("api/mobile/register", body, LoginResponse::class.java)
    }

    fun getProfile(): ProfileResponse {
        return get("api/mobile/me", ProfileResponse::class.java)
    }

    fun getLatestReadings(): LatestReadingsResponse {
        return get("api/mobile/health-sync", LatestReadingsResponse::class.java)
    }

    fun syncReadings(payload: HealthSyncPayload): SyncResponse {
        val builder = FormBody.Builder()
            .add("source", payload.source ?: "HEALTH_CONNECT")

        payload.heartRate?.let { builder.add("heartRate", it.toString()) }
        payload.temperature?.let { builder.add("temperature", String.format(Locale.US, "%.2f", it)) }
        payload.systolic?.let { builder.add("systolic", it.toString()) }
        payload.diastolic?.let { builder.add("diastolic", it.toString()) }
        payload.recordedAt?.let { builder.add("recordedAt", it) }
        payload.externalRecordId?.let { builder.add("externalRecordId", it) }
        payload.deviceType?.let { builder.add("deviceType", it) }
        payload.deviceManufacturer?.let { builder.add("deviceManufacturer", it) }
        payload.deviceModel?.let { builder.add("deviceModel", it) }

        return post("api/mobile/health-sync", builder.build(), SyncResponse::class.java)
    }

    fun syncHealthSection(payload: HealthSectionSyncPayload): SyncResponse {
        val builder = FormBody.Builder()
            .add("windowStart", payload.windowStart)
            .add("windowEnd", payload.windowEnd)
            .add("source", payload.source)
            .add("heartRateCount", payload.heartRateCount.toString())
            .add("temperatureCount", payload.temperatureCount.toString())
            .add("bloodPressureCount", payload.bloodPressureCount.toString())

        payload.heartRateLatest?.let { builder.add("heartRateLatest", it.toString()) }
        payload.heartRateMin?.let { builder.add("heartRateMin", it.toString()) }
        payload.heartRateMax?.let { builder.add("heartRateMax", it.toString()) }
        payload.heartRateAverage?.let { builder.add("heartRateAverage", String.format(Locale.US, "%.2f", it)) }
        payload.temperatureLatest?.let { builder.add("temperatureLatest", String.format(Locale.US, "%.2f", it)) }
        payload.temperatureMin?.let { builder.add("temperatureMin", String.format(Locale.US, "%.2f", it)) }
        payload.temperatureMax?.let { builder.add("temperatureMax", String.format(Locale.US, "%.2f", it)) }
        payload.temperatureAverage?.let { builder.add("temperatureAverage", String.format(Locale.US, "%.2f", it)) }
        payload.systolicLatest?.let { builder.add("systolicLatest", it.toString()) }
        payload.diastolicLatest?.let { builder.add("diastolicLatest", it.toString()) }
        payload.deviceType?.let { builder.add("deviceType", it) }
        payload.deviceManufacturer?.let { builder.add("deviceManufacturer", it) }
        payload.deviceModel?.let { builder.add("deviceModel", it) }

        return post("api/mobile/health-section-sync", builder.build(), SyncResponse::class.java)
    }

    fun logout(): SyncResponse {
        return post("api/mobile/logout", FormBody.Builder().build(), SyncResponse::class.java)
    }

    fun updateProfile(
        title: String,
        firstName: String,
        surname: String,
        dob: String,
        gender: String,
        maritalStatus: String,
        cellNumber: String,
        idNumber: String,
        emergencyContactName: String,
        emergencyContactNumber: String,
        bloodGroup: String,
        knownAllergies: String,
        chronicConditions: String,
        address: String
    ): SyncResponse {
        val body = FormBody.Builder()
            .add("title", title)
            .add("firstName", firstName)
            .add("surname", surname)
            .add("dob", dob)
            .add("gender", gender)
            .add("maritalStatus", maritalStatus)
            .add("cellNumber", cellNumber)
            .add("idNumber", idNumber)
            .add("emergencyContactName", emergencyContactName)
            .add("emergencyContactNumber", emergencyContactNumber)
            .add("bloodGroup", bloodGroup)
            .add("knownAllergies", knownAllergies)
            .add("chronicConditions", chronicConditions)
            .add("address", address)
            .build()

        return post("api/mobile/me", body, SyncResponse::class.java)
    }

    fun chatWithAi(message: String, vitals: String, history: String): AiChatResponse {
        val body = FormBody.Builder()
            .add("message", message)
            .add("vitals", vitals)
            .add("history", history)
            .build()

        return post("AIChatServlet.do", body, AiChatResponse::class.java)
    }

    fun clearSession() {
        cookieJar.clear()
    }

    private fun <T> get(path: String, responseClass: Class<T>): T {
        val request = Request.Builder()
            .url(BuildConfig.BASE_URL + path)
            .get()
            .build()

        execute(request).use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(extractMessage(body, response.code))
            }
            return parse(body, responseClass)
        }
    }

    private fun <T> post(path: String, body: FormBody, responseClass: Class<T>): T {
        val request = Request.Builder()
            .url(BuildConfig.BASE_URL + path)
            .post(body)
            .build()

        execute(request).use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(extractMessage(responseBody, response.code))
            }
            return parse(responseBody, responseClass)
        }
    }

    private fun execute(request: Request): okhttp3.Response {
        try {
            return client.newCall(request).execute()
        } catch (e: IOException) {
            throw IllegalStateException(
                "Cannot reach the online SmartHealth service. Check your internet connection and make sure this is the latest app version."
            )
        }
    }

    private fun <T> parse(body: String, responseClass: Class<T>): T {
        try {
            return gson.fromJson(body, responseClass)
        } catch (e: JsonSyntaxException) {
            throw IllegalStateException("Unexpected server response.")
        }
    }

    private fun extractMessage(body: String, statusCode: Int): String {
        if (body.isBlank()) {
            return "The server returned HTTP $statusCode with no message."
        }

        return try {
            val parsed = gson.fromJson(body, SyncResponse::class.java)
            parsed.message ?: "The server returned HTTP $statusCode."
        } catch (_: Exception) {
            "The server returned HTTP $statusCode. Please update the app and try again."
        }
    }
}
