package za.ac.tut.healthmonitor.mobile.data.repository

import za.ac.tut.healthmonitor.mobile.data.api.BackendApiClient
import za.ac.tut.healthmonitor.mobile.data.model.HealthSectionSyncPayload
import za.ac.tut.healthmonitor.mobile.data.model.HealthSyncPayload
import za.ac.tut.healthmonitor.mobile.data.model.LatestReadingsResponse
import za.ac.tut.healthmonitor.mobile.data.model.LoginResponse
import za.ac.tut.healthmonitor.mobile.data.model.ProfileResponse
import za.ac.tut.healthmonitor.mobile.data.model.SyncResponse

class AppRepository(
    private val backendApiClient: BackendApiClient = BackendApiClient()
) {

    fun login(email: String, password: String): LoginResponse {
        return backendApiClient.login(email, password)
    }

    fun register(
        firstName: String,
        surname: String,
        email: String,
        password: String
    ): LoginResponse {
        return backendApiClient.register(
            firstName = firstName,
            surname = surname,
            email = email,
            password = password
        )
    }

    fun getProfile(): ProfileResponse {
        return backendApiClient.getProfile()
    }

    fun getLatestReadings(): LatestReadingsResponse {
        return backendApiClient.getLatestReadings()
    }

    fun syncReadings(payload: HealthSyncPayload): SyncResponse {
        return backendApiClient.syncReadings(payload)
    }

    fun syncHealthSection(payload: HealthSectionSyncPayload): SyncResponse {
        return backendApiClient.syncHealthSection(payload)
    }

    fun logout(): SyncResponse {
        return backendApiClient.logout()
    }

    fun updateProfile(
        title: String,
        firstName: String,
        surname: String,
        gender: String,
        cellNumber: String
    ): SyncResponse {
        return backendApiClient.updateProfile(
            title = title,
            firstName = firstName,
            surname = surname,
            gender = gender,
            cellNumber = cellNumber
        )
    }

    fun chatWithAi(message: String, vitals: String, history: String): String {
        return backendApiClient.chatWithAi(message, vitals, history).reply
            ?: "I could not generate a reply right now."
    }

    fun clearSession() {
        backendApiClient.clearSession()
    }
}
