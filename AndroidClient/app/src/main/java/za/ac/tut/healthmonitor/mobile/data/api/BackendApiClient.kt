package za.ac.tut.healthmonitor.mobile.data.api

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale
import za.ac.tut.healthmonitor.mobile.BuildConfig
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
        title: String,
        firstName: String,
        surname: String,
        dob: String,
        gender: String,
        maritalStatus: String,
        email: String,
        cellNumber: String,
        address: String,
        password: String
    ): LoginResponse {
        val body = FormBody.Builder()
            .add("title", title)
            .add("firstName", firstName)
            .add("surname", surname)
            .add("dob", dob)
            .add("gender", gender)
            .add("maritalStatus", maritalStatus)
            .add("email", email)
            .add("cellNumber", cellNumber)
            .add("address", address)
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
            .add("source", "HEALTH_CONNECT")

        payload.heartRate?.let { builder.add("heartRate", it.toString()) }
        payload.temperature?.let { builder.add("temperature", String.format(Locale.US, "%.2f", it)) }
        payload.systolic?.let { builder.add("systolic", it.toString()) }
        payload.diastolic?.let { builder.add("diastolic", it.toString()) }

        return post("api/mobile/health-sync", builder.build(), SyncResponse::class.java)
    }

    fun logout(): SyncResponse {
        return post("api/mobile/logout", FormBody.Builder().build(), SyncResponse::class.java)
    }

    fun clearSession() {
        cookieJar.clear()
    }

    private fun <T> get(path: String, responseClass: Class<T>): T {
        val request = Request.Builder()
            .url(BuildConfig.BASE_URL + path)
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(extractMessage(body))
            }
            return parse(body, responseClass)
        }
    }

    private fun <T> post(path: String, body: FormBody, responseClass: Class<T>): T {
        val request = Request.Builder()
            .url(BuildConfig.BASE_URL + path)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(extractMessage(responseBody))
            }
            return parse(responseBody, responseClass)
        }
    }

    private fun <T> parse(body: String, responseClass: Class<T>): T {
        try {
            return gson.fromJson(body, responseClass)
        } catch (e: JsonSyntaxException) {
            throw IllegalStateException("Unexpected server response.")
        }
    }

    private fun extractMessage(body: String): String {
        if (body.isBlank()) {
            return "The server returned an empty response."
        }

        return try {
            val parsed = gson.fromJson(body, SyncResponse::class.java)
            parsed.message ?: "The request could not be completed."
        } catch (_: Exception) {
            "The request could not be completed."
        }
    }
}
