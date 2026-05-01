package za.ac.tut.healthmonitor.mobile.ui

import za.ac.tut.healthmonitor.mobile.data.model.BackendProfile
import za.ac.tut.healthmonitor.mobile.data.model.LatestReadingsResponse

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

data class AppUiState(
    val email: String = "",
    val password: String = "",
    val signupTitle: String = "",
    val signupFirstName: String = "",
    val signupSurname: String = "",
    val signupDob: String = "",
    val signupGender: String = "",
    val signupMaritalStatus: String = "",
    val signupEmail: String = "",
    val signupCellNumber: String = "",
    val signupAddress: String = "",
    val signupPassword: String = "",
    val signupConfirmPassword: String = "",
    val authScreen: AuthScreen = AuthScreen.Login,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userProfile: BackendProfile? = null,
    val latestReadings: LatestReadingsResponse? = null,
    val trendPoints: List<VitalTrendPoint> = emptyList(),
    val isLiveSyncEnabled: Boolean = false,
    val isDemoSyncEnabled: Boolean = false,
    val lastLiveSyncAt: String? = null,
    val liveSyncIntervalSeconds: Int = 30,
    val demoSyncIntervalSeconds: Int = 5,
    val selectedLanguage: String = "en",
    val isProfileOpen: Boolean = false,
    val editTitle: String = "",
    val editFirstName: String = "",
    val editSurname: String = "",
    val editGender: String = "",
    val editCellNumber: String = "",
    val isChatOpen: Boolean = false,
    val chatInput: String = "",
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(false, "Hello! How can I assist you with your health today?")
    ),
    val alertSent: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)
