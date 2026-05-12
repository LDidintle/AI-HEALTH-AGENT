package za.ac.tut.healthmonitor.mobile.data.repository

import za.ac.tut.healthmonitor.mobile.data.api.BackendApiClient
import za.ac.tut.healthmonitor.mobile.data.model.AlertNotificationResponse
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
        dob: String,
        password: String
    ): LoginResponse {
        return backendApiClient.register(
            firstName = firstName,
            surname = surname,
            email = email,
            dob = dob,
            password = password
        )
    }

    fun getProfile(): ProfileResponse {
        return backendApiClient.getProfile()
    }

    fun getLatestReadings(email: String? = null): LatestReadingsResponse {
        return backendApiClient.getLatestReadings(email)
    }

    fun getAlertNotification(email: String? = null): AlertNotificationResponse {
        return backendApiClient.getAlertNotification(email)
    }

    fun syncReadings(payload: HealthSyncPayload, email: String? = null): SyncResponse {
        return backendApiClient.syncReadings(payload, email)
    }

    fun syncHealthSection(payload: HealthSectionSyncPayload, email: String? = null): SyncResponse {
        return backendApiClient.syncHealthSection(payload, email)
    }

    fun logout(): SyncResponse {
        return backendApiClient.logout()
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
        return backendApiClient.updateProfile(
            title = title,
            firstName = firstName,
            surname = surname,
            dob = dob,
            gender = gender,
            maritalStatus = maritalStatus,
            cellNumber = cellNumber,
            idNumber = idNumber,
            emergencyContactName = emergencyContactName,
            emergencyContactNumber = emergencyContactNumber,
            bloodGroup = bloodGroup,
            knownAllergies = knownAllergies,
            chronicConditions = chronicConditions,
            address = address
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
