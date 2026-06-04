package za.ac.tut.healthmonitor.mobile.ui

import za.ac.tut.healthmonitor.mobile.data.model.BackendProfile
import za.ac.tut.healthmonitor.mobile.data.model.EmergencyAlertNotification
import za.ac.tut.healthmonitor.mobile.data.model.LatestReadingsResponse
import za.ac.tut.healthmonitor.mobile.insights.ActivityState

enum class AuthScreen {
    Login,
    Signup,
    HowItWorks
}

data class ChatMessage(
    val fromUser: Boolean,
    val text: String
)

data class VitalTrendPoint(
    val heartRate: Int?,
    val systolic: Int?,
    val temperature: Double?
)

data class HeartRateRangeSummary(
    val latest: Int?,
    val min: Int?,
    val max: Int?,
    val average: Double?,
    val count: Int,
    val windowEnd: String?,
    val source: String?
)

data class AppUiState(
    val email: String = "",
    val password: String = "",
    val signupFirstName: String = "",
    val signupSurname: String = "",
    val signupEmail: String = "",
    val signupDob: String = "",
    val signupPassword: String = "",
    val signupConfirmPassword: String = "",
    val authScreen: AuthScreen = AuthScreen.Login,
    val isLoading: Boolean = false,
    val isRestoringSession: Boolean = true,
    val isLoggedIn: Boolean = false,
    val userProfile: BackendProfile? = null,
    val latestReadings: LatestReadingsResponse? = null,
    val heartRateRange: HeartRateRangeSummary? = null,
    val readingContext: ActivityState = ActivityState.NotSure,
    val sleepStart: String = "22:00",
    val sleepEnd: String = "06:30",
    val trendPoints: List<VitalTrendPoint> = emptyList(),
    val lastSectionSyncAt: String? = null,
    val lastSyncSummary: String? = null,
    val selectedLanguage: String = "en",
    val isProfileOpen: Boolean = false,
    val editTitle: String = "",
    val editFirstName: String = "",
    val editSurname: String = "",
    val editDob: String = "",
    val editGender: String = "",
    val editMaritalStatus: String = "",
    val editCellNumber: String = "",
    val editIdNumber: String = "",
    val editEmergencyContactName: String = "",
    val editEmergencyContactNumber: String = "",
    val editBloodGroup: String = "",
    val editKnownAllergies: String = "",
    val editChronicConditions: String = "",
    val editAddress: String = "",
    val isChatOpen: Boolean = false,
    val chatInput: String = "",
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(false, "Hello! How can I assist you with your health today?")
    ),
    val alertSent: Boolean = false,
    val activeAlert: EmergencyAlertNotification? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)
