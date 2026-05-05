package za.ac.tut.healthmonitor.mobile.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import za.ac.tut.healthmonitor.mobile.data.model.BloodPressureValue
import za.ac.tut.healthmonitor.mobile.data.model.HealthSectionSyncPayload
import za.ac.tut.healthmonitor.mobile.data.model.LatestReadingsResponse
import za.ac.tut.healthmonitor.mobile.data.model.ReadingValue
import za.ac.tut.healthmonitor.mobile.data.model.TemperatureValue
import za.ac.tut.healthmonitor.mobile.data.repository.AppRepository
import za.ac.tut.healthmonitor.mobile.health.HealthSection
import za.ac.tut.healthmonitor.mobile.health.HealthSectionTrendPoint
import za.ac.tut.healthmonitor.mobile.health.HealthConnectManager

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository()
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()
    private var demoTick = 0

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun showLogin() {
        _uiState.update { it.copy(authScreen = AuthScreen.Login, errorMessage = null, infoMessage = null) }
    }

    fun showSignup() {
        _uiState.update { it.copy(authScreen = AuthScreen.Signup, errorMessage = null, infoMessage = null) }
    }

    fun showHowItWorks() {
        _uiState.update { it.copy(authScreen = AuthScreen.HowItWorks, errorMessage = null, infoMessage = null) }
    }

    fun updateSignupFirstName(value: String) {
        _uiState.update { it.copy(signupFirstName = value) }
    }

    fun updateSignupSurname(value: String) {
        _uiState.update { it.copy(signupSurname = value) }
    }

    fun updateSignupEmail(value: String) {
        _uiState.update { it.copy(signupEmail = value) }
    }

    fun updateSignupPassword(value: String) {
        _uiState.update { it.copy(signupPassword = value) }
    }

    fun updateSignupConfirmPassword(value: String) {
        _uiState.update { it.copy(signupConfirmPassword = value) }
    }

    fun login() {
        val currentState = _uiState.value
        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter both email and password.") }
            return
        }

        launchLoadingTask {
            repository.login(currentState.email.trim(), currentState.password)
            val profile = repository.getProfile().user
            val verificationMessage = if (profile?.isVerified == true) {
                "Signed in. Sync latest section to view readings."
            } else {
                "Signed in. Your account is pending staff verification."
            }

            _uiState.update {
                it.copy(
                    isLoggedIn = true,
                    userProfile = profile,
                    latestReadings = null,
                    trendPoints = emptyList(),
                    lastSectionSyncAt = null,
                    lastSyncSummary = null,
                    password = "",
                    errorMessage = null,
                    infoMessage = verificationMessage
                )
            }
        }
    }

    fun register() {
        val currentState = _uiState.value
        val requiredFields = listOf(
            currentState.signupFirstName,
            currentState.signupSurname,
            currentState.signupEmail,
            currentState.signupPassword,
            currentState.signupConfirmPassword
        )

        if (requiredFields.any { it.isBlank() }) {
            _uiState.update { it.copy(errorMessage = "Complete the required account fields.") }
            return
        }

        if (currentState.signupPassword != currentState.signupConfirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }

        launchLoadingTask {
            repository.register(
                firstName = currentState.signupFirstName.trim(),
                surname = currentState.signupSurname.trim(),
                email = currentState.signupEmail.trim(),
                password = currentState.signupPassword
            )

            _uiState.update {
                it.copy(
                    authScreen = AuthScreen.Login,
                    email = currentState.signupEmail.trim(),
                    password = "",
                    signupPassword = "",
                    signupConfirmPassword = "",
                    errorMessage = null,
                    infoMessage = "Account created. Staff can verify it from the patient directory."
                )
            }
        }
    }

    fun refreshDashboard() {
        launchLoadingTask {
            val profile = repository.getProfile().user

            _uiState.update {
                it.copy(
                    isLoggedIn = true,
                    userProfile = profile,
                    latestReadings = null,
                    trendPoints = emptyList(),
                    lastSectionSyncAt = null,
                    lastSyncSummary = null,
                    infoMessage = "Dashboard cleared. Sync latest section to view readings.",
                    errorMessage = null
                )
            }
        }
    }

    fun syncLatestSection(manager: HealthConnectManager) {
        executeLatestSectionSync(manager)
    }

    fun startDemoSync() {
        viewModelScope.launch {
            val section = buildDemoSection()
            val readings = section.payload.toLatestReadings()
            persistSection(section.payload)
            _uiState.update {
                it.copy(
                    latestReadings = readings,
                    trendPoints = section.trendPoints.toUiTrendPoints(),
                    lastSectionSyncAt = formattedNow(),
                    lastSyncSummary = sectionSummary(section.payload),
                    errorMessage = null,
                    infoMessage = "Demo section loaded."
                )
            }
            demoTick += 1
        }
    }

    fun logout() {
        clearSectionState(showMessage = false)
        launchLoadingTask {
            repository.logout()
            repository.clearSession()
            _uiState.value = AppUiState(
                email = _uiState.value.email,
                infoMessage = "Signed out."
            )
        }
    }

    fun setInfoMessage(message: String) {
        _uiState.update { it.copy(infoMessage = message, errorMessage = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(infoMessage = null, errorMessage = null) }
    }

    fun selectLanguage(language: String) {
        _uiState.update { it.copy(selectedLanguage = language) }
    }

    fun openProfile() {
        val profile = _uiState.value.userProfile ?: return
        _uiState.update {
            it.copy(
                isProfileOpen = true,
                editTitle = profile.title.orEmpty(),
                editFirstName = profile.firstName,
                editSurname = profile.surname,
                editGender = profile.gender.orEmpty(),
                editCellNumber = profile.cellNumber.orEmpty(),
                errorMessage = null,
                infoMessage = null
            )
        }
    }

    fun closeProfile() {
        _uiState.update { it.copy(isProfileOpen = false) }
    }

    fun updateEditTitle(value: String) {
        _uiState.update { it.copy(editTitle = value) }
    }

    fun updateEditFirstName(value: String) {
        _uiState.update { it.copy(editFirstName = value) }
    }

    fun updateEditSurname(value: String) {
        _uiState.update { it.copy(editSurname = value) }
    }

    fun updateEditGender(value: String) {
        _uiState.update { it.copy(editGender = value) }
    }

    fun updateEditCellNumber(value: String) {
        _uiState.update { it.copy(editCellNumber = value) }
    }

    fun saveProfile() {
        val state = _uiState.value
        if (state.editFirstName.isBlank() || state.editSurname.isBlank()) {
            _uiState.update { it.copy(errorMessage = "First name and surname are required.") }
            return
        }

        launchLoadingTask {
            repository.updateProfile(
                title = state.editTitle.ifBlank { "Patient" },
                firstName = state.editFirstName.trim(),
                surname = state.editSurname.trim(),
                gender = state.editGender.ifBlank { "Not specified" },
                cellNumber = state.editCellNumber.trim()
            )
            val profile = repository.getProfile().user
            _uiState.update {
                it.copy(
                    userProfile = profile,
                    isProfileOpen = false,
                    errorMessage = null,
                    infoMessage = "Profile updated."
                )
            }
        }
    }

    fun openChat() {
        _uiState.update { it.copy(isChatOpen = true, errorMessage = null, infoMessage = null) }
    }

    fun closeChat() {
        _uiState.update { it.copy(isChatOpen = false) }
    }

    fun updateChatInput(value: String) {
        _uiState.update { it.copy(chatInput = value) }
    }

    fun sendChatMessage() {
        val state = _uiState.value
        val message = state.chatInput.trim()
        if (message.isEmpty()) {
            return
        }

        val userMessage = ChatMessage(true, message)
        val thinkingMessage = ChatMessage(false, "Thinking...")
        _uiState.update {
            it.copy(
                chatInput = "",
                chatMessages = it.chatMessages + userMessage + thinkingMessage,
                errorMessage = null,
                infoMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val reply = withContext(Dispatchers.IO) {
                    repository.chatWithAi(
                        message = message,
                        vitals = vitalsJson(_uiState.value.latestReadings),
                        history = chatHistory(_uiState.value.chatMessages)
                    )
                }

                _uiState.update {
                    it.copy(chatMessages = it.chatMessages.dropLast(1) + ChatMessage(false, reply))
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        chatMessages = it.chatMessages.dropLast(1) + ChatMessage(
                            false,
                            "I could not reach the AI assistant right now. Keep monitoring your readings and contact doctor/staff if symptoms feel worrying."
                        )
                    )
                }
            }
        }
    }

    fun sendParamedicAlert() {
        _uiState.update {
            it.copy(
                alertSent = true,
                infoMessage = "Emergency alert shown. If this is serious, call emergency services now.",
                errorMessage = null
            )
        }
    }

    private fun chatHistory(messages: List<ChatMessage>): String {
        return messages.takeLast(8).joinToString("\n") {
            (if (it.fromUser) "User: " else "Assistant: ") + it.text
        }
    }

    private fun vitalsJson(readings: LatestReadingsResponse?): String {
        val heartRate = readings?.heartRate?.value?.toString() ?: "null"
        val temperature = readings?.temperature?.value?.toString() ?: "null"
        val bloodPressure = readings?.bloodPressure?.let {
            "\"${it.systolic}/${it.diastolic}\""
        } ?: "null"

        return "{\"heartRate\":$heartRate,\"temperature\":$temperature,\"bloodPressure\":$bloodPressure}"
    }

    override fun onCleared() {
        super.onCleared()
    }

    private fun clearSectionState(showMessage: Boolean) {
        _uiState.update {
            it.copy(
                latestReadings = null,
                trendPoints = emptyList(),
                lastSectionSyncAt = null,
                lastSyncSummary = null,
                infoMessage = if (showMessage) "Section view cleared." else it.infoMessage,
                errorMessage = null
            )
        }
    }

    private fun executeLatestSectionSync(manager: HealthConnectManager) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            try {
                val section = withContext(Dispatchers.IO) {
                    if (!manager.hasAnyPermission()) {
                        throw SecurityException("Health Connect permissions are required to sync a section.")
                    }
                    manager.readLatestSection(DEFAULT_SECTION_WINDOW_MINUTES)
                }

                if (section.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            latestReadings = null,
                            trendPoints = emptyList(),
                            lastSectionSyncAt = formattedNow(),
                            lastSyncSummary = null,
                            errorMessage = null,
                            infoMessage = "No Health Connect records found in the last 60 minutes."
                        )
                    }
                    return@launch
                }

                val readings = section.payload.toLatestReadings()
                persistSection(section.payload)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        latestReadings = readings,
                        trendPoints = section.trendPoints.toUiTrendPoints(),
                        lastSectionSyncAt = formattedNow(),
                        lastSyncSummary = sectionSummary(section.payload),
                        errorMessage = null,
                        infoMessage = "Latest 60-minute section synced."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        latestReadings = null,
                        trendPoints = emptyList(),
                        lastSyncSummary = null,
                        errorMessage = e.message ?: "Health section sync failed.",
                        infoMessage = null
                    )
                }
            }
        }
    }

    private fun buildDemoSection(): HealthSection {
        val end = Instant.now()
        val start = end.minusSeconds(60 * DEFAULT_SECTION_WINDOW_MINUTES)
        val trendPoints = (0 until MAX_TREND_POINTS).map { index ->
            val wave = sin((demoTick + index) / 2.0)
            val smallWave = sin((demoTick + index) / 3.0)
            HealthSectionTrendPoint(
                heartRate = (78 + wave * 8 + Random.nextInt(-2, 3)).roundToInt().coerceIn(58, 112),
                systolic = (124 + smallWave * 6 + Random.nextInt(-2, 3)).roundToInt().coerceIn(105, 145),
                temperature = 36.7 + smallWave * 0.25 + Random.nextDouble(-0.08, 0.09)
            )
        }

        val heartRates = trendPoints.mapNotNull { it.heartRate }
        val systolicLatest = trendPoints.lastOrNull()?.systolic
        val diastolicLatest = systolicLatest?.let { (it - 46).coerceIn(65, 95) }
        val temperatures = trendPoints.mapNotNull { it.temperature }

        return HealthSection(
            payload = HealthSectionSyncPayload(
                windowStart = start.toString(),
                windowEnd = end.toString(),
                source = "DEMO_SECTION",
                heartRateLatest = heartRates.lastOrNull(),
                heartRateMin = heartRates.minOrNull(),
                heartRateMax = heartRates.maxOrNull(),
                heartRateAverage = heartRates.takeIf { it.isNotEmpty() }?.average(),
                heartRateCount = heartRates.size,
                temperatureLatest = temperatures.lastOrNull(),
                temperatureMin = temperatures.minOrNull(),
                temperatureMax = temperatures.maxOrNull(),
                temperatureAverage = temperatures.takeIf { it.isNotEmpty() }?.average(),
                temperatureCount = temperatures.size,
                systolicLatest = systolicLatest,
                diastolicLatest = diastolicLatest,
                bloodPressureCount = if (systolicLatest != null && diastolicLatest != null) 1 else 0,
                deviceType = "WATCH",
                deviceManufacturer = "Samsung",
                deviceModel = "Galaxy Watch 5 Demo"
            ),
            trendPoints = trendPoints
        )
    }

    private fun persistSection(payload: HealthSectionSyncPayload) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.syncHealthSection(payload)
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Section displayed, but saving to the database failed.")
                }
            }
        }
    }

    private fun HealthSectionSyncPayload.toLatestReadings(): LatestReadingsResponse {
        return LatestReadingsResponse(
            success = true,
            heartRate = heartRateLatest?.let {
                ReadingValue(
                    value = it,
                    recordedAt = windowEnd,
                    source = source
                )
            },
            temperature = temperatureLatest?.let {
                TemperatureValue(
                    value = it,
                    recordedAt = windowEnd,
                    source = source
                )
            },
            bloodPressure = if (systolicLatest != null && diastolicLatest != null) {
                BloodPressureValue(
                    systolic = systolicLatest,
                    diastolic = diastolicLatest,
                    recordedAt = windowEnd,
                    source = source
                )
            } else {
                null
            }
        )
    }

    private fun sectionSummary(payload: HealthSectionSyncPayload): String {
        return "Section: " +
                "Heart ${payload.heartRateLatest?.let { "$it BPM" } ?: "none"} (${payload.heartRateCount}), " +
                "BP ${formatBloodPressure(payload.systolicLatest, payload.diastolicLatest)} (${payload.bloodPressureCount}), " +
                "Temp ${payload.temperatureLatest?.let { String.format(java.util.Locale.US, "%.2f °C", it) } ?: "none"} (${payload.temperatureCount})"
    }

    private fun List<HealthSectionTrendPoint>.toUiTrendPoints(): List<VitalTrendPoint> {
        return map {
            VitalTrendPoint(
                heartRate = it.heartRate,
                systolic = it.systolic,
                temperature = it.temperature
            )
        }
    }

    private fun formatBloodPressure(systolic: Int?, diastolic: Int?): String {
        return if (systolic == null || diastolic == null) {
            "none"
        } else {
            "$systolic/$diastolic"
        }
    }

    private fun formattedNow(): String {
        return LocalTime.now().format(TIME_FORMATTER)
    }

    private fun launchLoadingTask(block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            try {
                withContext(Dispatchers.IO) {
                    block()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: "Something went wrong.",
                        isLoading = false
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private companion object {
        const val DEFAULT_SECTION_WINDOW_MINUTES = 60L
        const val MAX_TREND_POINTS = 12
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
