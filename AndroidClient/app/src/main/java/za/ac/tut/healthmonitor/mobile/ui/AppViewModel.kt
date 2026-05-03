package za.ac.tut.healthmonitor.mobile.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import za.ac.tut.healthmonitor.mobile.data.model.BloodPressureValue
import za.ac.tut.healthmonitor.mobile.data.model.HealthSyncPayload
import za.ac.tut.healthmonitor.mobile.data.model.LatestReadingsResponse
import za.ac.tut.healthmonitor.mobile.data.model.ReadingValue
import za.ac.tut.healthmonitor.mobile.data.model.TemperatureValue
import za.ac.tut.healthmonitor.mobile.data.repository.AppRepository
import za.ac.tut.healthmonitor.mobile.health.HealthConnectManager

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository()
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()
    private var liveSyncJob: Job? = null
    private var demoSyncJob: Job? = null
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

    fun updateSignupTitle(value: String) {
        _uiState.update { it.copy(signupTitle = value) }
    }

    fun updateSignupFirstName(value: String) {
        _uiState.update { it.copy(signupFirstName = value) }
    }

    fun updateSignupSurname(value: String) {
        _uiState.update { it.copy(signupSurname = value) }
    }

    fun updateSignupDob(value: String) {
        _uiState.update { it.copy(signupDob = value) }
    }

    fun updateSignupGender(value: String) {
        _uiState.update { it.copy(signupGender = value) }
    }

    fun updateSignupMaritalStatus(value: String) {
        _uiState.update { it.copy(signupMaritalStatus = value) }
    }

    fun updateSignupEmail(value: String) {
        _uiState.update { it.copy(signupEmail = value) }
    }

    fun updateSignupCellNumber(value: String) {
        _uiState.update { it.copy(signupCellNumber = value) }
    }

    fun updateSignupAddress(value: String) {
        _uiState.update { it.copy(signupAddress = value) }
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

            _uiState.update {
                it.copy(
                    isLoggedIn = true,
                    userProfile = profile,
                    latestReadings = null,
                    trendPoints = emptyList(),
                    lastLiveSyncAt = null,
                    lastSyncSummary = null,
                    password = "",
                    errorMessage = null,
                    infoMessage = "Signed in. Start live watch sync to show current readings."
                )
            }
        }
    }

    fun register() {
        val currentState = _uiState.value
        val requiredFields = listOf(
            currentState.signupFirstName,
            currentState.signupSurname,
            currentState.signupDob,
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
                title = currentState.signupTitle.ifBlank { "Patient" },
                firstName = currentState.signupFirstName.trim(),
                surname = currentState.signupSurname.trim(),
                dob = currentState.signupDob.trim(),
                gender = currentState.signupGender.ifBlank { "Not specified" },
                maritalStatus = currentState.signupMaritalStatus.ifBlank { "Not specified" },
                email = currentState.signupEmail.trim(),
                cellNumber = currentState.signupCellNumber.trim(),
                address = currentState.signupAddress.trim(),
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
                    infoMessage = "Account created. You can now sign in."
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
                    lastLiveSyncAt = null,
                    lastSyncSummary = null,
                    infoMessage = "Dashboard cleared. Start live watch sync to show current readings.",
                    errorMessage = null
                )
            }
        }
    }

    fun syncFromHealthConnect(manager: HealthConnectManager) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            try {
                val payload = withContext(Dispatchers.IO) {
                    manager.readLatestVitals()
                }
                if (payload.isEmpty()) {
                    throw IllegalStateException(
                        "Health Connect returned no readings. Check Samsung Health is sharing data into Health Connect."
                    )
                }
                val readings = payload.toLatestReadings()
                persistPayload(payload)
                _uiState.update {
                    it.copy(
                        latestReadings = readings,
                        trendPoints = appendTrendPoint(it.trendPoints, readings),
                        lastLiveSyncAt = formattedNow(),
                        lastSyncSummary = healthConnectSummary(payload),
                        isLoading = false,
                        errorMessage = null,
                        infoMessage = "Health Connect values displayed. Saving to database in the background."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Health Connect sync failed.",
                        infoMessage = null
                    )
                }
            }
        }
    }

    fun startLiveSync(manager: HealthConnectManager) {
        if (liveSyncJob?.isActive == true) {
            _uiState.update {
                it.copy(infoMessage = "Live watch sync is already running.", errorMessage = null)
            }
            return
        }

        stopDemoSync(showMessage = false)
        liveSyncJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLiveSyncEnabled = true,
                    isDemoSyncEnabled = false,
                    latestReadings = null,
                    trendPoints = emptyList(),
                    lastLiveSyncAt = null,
                    lastSyncSummary = null,
                    errorMessage = null,
                    infoMessage = "Live watch sync started. Reading directly from Health Connect every 5 seconds."
                )
            }

            while (isActive) {
                try {
                    val payload = withContext(Dispatchers.IO) {
                        if (!manager.hasAnyPermission()) {
                            throw SecurityException("Health Connect permissions are required for live watch sync.")
                        }

                        manager.readLatestVitals()
                    }

                    _uiState.update {
                        if (payload.isEmpty()) {
                            it.copy(
                                latestReadings = null,
                                trendPoints = emptyList(),
                                lastLiveSyncAt = formattedNow(),
                                lastSyncSummary = healthConnectSummary(payload),
                                errorMessage = null,
                                infoMessage =
                                "Live sync checked Health Connect, but no watch readings were returned."
                            )
                        } else {
                            val latestReadings = payload.toLatestReadings()
                            persistPayload(payload)
                            it.copy(
                                latestReadings = latestReadings,
                                trendPoints = appendTrendPoint(it.trendPoints, latestReadings),
                                lastLiveSyncAt = formattedNow(),
                                lastSyncSummary = healthConnectSummary(payload),
                                errorMessage = null,
                                infoMessage =
                                "Live watch readings synced."
                            )
                        }
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    liveSyncJob = null
                    _uiState.update {
                        it.copy(
                            isLiveSyncEnabled = false,
                            errorMessage = e.message ?: "Live watch sync stopped.",
                            infoMessage = null
                        )
                    }
                    break
                }

                delay(LIVE_SYNC_INTERVAL_MILLIS)
            }
        }
    }

    fun stopLiveSync() {
        stopLiveSync(showMessage = true)
    }

    fun applyWatchLiveReading(payload: HealthSyncPayload) {
        if (payload.isEmpty()) {
            return
        }

        val readings = payload.toLatestReadings()
        persistPayload(payload)
        _uiState.update {
            it.copy(
                latestReadings = readings,
                trendPoints = appendTrendPoint(it.trendPoints, readings),
                lastLiveSyncAt = formattedNow(),
                lastSyncSummary = "Galaxy Watch live: " + healthValuesSummary(payload),
                errorMessage = null,
                infoMessage = "Live Galaxy Watch reading received."
            )
        }
    }

    fun startDemoSync() {
        if (demoSyncJob?.isActive == true) {
            _uiState.update { it.copy(infoMessage = "Demo live watch feed is already running.", errorMessage = null) }
            return
        }

        stopLiveSync(showMessage = false)
        demoTick = 0
        demoSyncJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDemoSyncEnabled = true,
                    isLiveSyncEnabled = false,
                    errorMessage = null,
                    infoMessage = "Demo live watch feed started. The graph and vital markers will update every 5 seconds."
                )
            }

            while (isActive) {
                try {
                    val payload = buildDemoPayload()
                    val readings = payload.toLatestReadings()
                    persistPayload(payload)

                    _uiState.update {
                        it.copy(
                            latestReadings = readings,
                            trendPoints = appendTrendPoint(it.trendPoints, readings),
                            lastLiveSyncAt = formattedNow(),
                            lastSyncSummary = readingsSummary(readings),
                            errorMessage = null,
                            infoMessage = "Demo watch reading synced."
                        )
                    }
                    demoTick += 1
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    demoSyncJob = null
                    _uiState.update {
                        it.copy(
                            isDemoSyncEnabled = false,
                            errorMessage = e.message ?: "Demo live watch feed stopped.",
                            infoMessage = null
                        )
                    }
                    break
                }

                delay(DEMO_SYNC_INTERVAL_MILLIS)
            }
        }
    }

    fun stopDemoSync() {
        stopDemoSync(showMessage = true)
    }

    fun syncManualSample() {
        launchLoadingTask {
            val payload = HealthSyncPayload(
                heartRate = 82,
                temperature = 36.8,
                systolic = 126,
                diastolic = 81
            )

            val readings = payload.toLatestReadings()
            persistPayload(payload)

            _uiState.update {
                it.copy(
                    latestReadings = readings,
                    trendPoints = appendTrendPoint(it.trendPoints, readings),
                    lastSyncSummary = readingsSummary(readings),
                    errorMessage = null,
                    infoMessage = "Sample vitals synced."
                )
            }
        }
    }

    fun logout() {
        stopLiveSync(showMessage = false)
        stopDemoSync(showMessage = false)
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
        liveSyncJob?.cancel()
        demoSyncJob?.cancel()
        super.onCleared()
    }

    private fun stopLiveSync(showMessage: Boolean) {
        liveSyncJob?.cancel()
        liveSyncJob = null
        _uiState.update {
            it.copy(
                isLiveSyncEnabled = false,
                latestReadings = null,
                trendPoints = emptyList(),
                lastLiveSyncAt = null,
                lastSyncSummary = null,
                infoMessage = if (showMessage) "Live watch sync stopped." else it.infoMessage,
                errorMessage = null
            )
        }
    }

    private fun stopDemoSync(showMessage: Boolean) {
        demoSyncJob?.cancel()
        demoSyncJob = null
        _uiState.update {
            it.copy(
                isDemoSyncEnabled = false,
                infoMessage = if (showMessage) "Demo live watch feed stopped." else it.infoMessage,
                errorMessage = null
            )
        }
    }

    private fun buildDemoPayload(): HealthSyncPayload {
        val wave = sin(demoTick / 2.0)
        val smallWave = sin(demoTick / 3.0)
        val heartRate = (78 + wave * 8 + Random.nextInt(-2, 3)).roundToInt().coerceIn(58, 112)
        val systolic = (124 + smallWave * 6 + Random.nextInt(-2, 3)).roundToInt().coerceIn(105, 145)
        val diastolic = (78 + smallWave * 4 + Random.nextInt(-1, 2)).roundToInt().coerceIn(65, 95)
        val temperature = 36.7 + smallWave * 0.25 + Random.nextDouble(-0.08, 0.09)

        return HealthSyncPayload(
            heartRate = heartRate,
            temperature = temperature,
            systolic = systolic,
            diastolic = diastolic,
            source = "DEMO_WATCH",
            recordedAt = Instant.now().toString(),
            externalRecordId = "demo-watch-${Instant.now().toEpochMilli()}",
            deviceType = "WATCH",
            deviceManufacturer = "Samsung",
            deviceModel = "Galaxy Watch 5 Demo"
        )
    }

    private fun persistPayload(payload: HealthSyncPayload) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.syncReadings(payload)
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Live value displayed, but saving to the database failed.")
                }
            }
        }
    }

    private fun HealthSyncPayload.toLatestReadings(): LatestReadingsResponse {
        val measuredAt = recordedAt ?: Instant.now().toString()
        val readingSource = source ?: "HEALTH_CONNECT"

        return LatestReadingsResponse(
            success = true,
            heartRate = heartRate?.let {
                ReadingValue(
                    value = it,
                    recordedAt = measuredAt,
                    source = readingSource
                )
            },
            temperature = temperature?.let {
                TemperatureValue(
                    value = it,
                    recordedAt = measuredAt,
                    source = readingSource
                )
            },
            bloodPressure = if (systolic != null && diastolic != null) {
                BloodPressureValue(
                    systolic = systolic,
                    diastolic = diastolic,
                    recordedAt = measuredAt,
                    source = readingSource
                )
            } else {
                null
            }
        )
    }

    private fun healthConnectSummary(payload: HealthSyncPayload): String {
        return "Health Connect returned: " + healthValuesSummary(payload)
    }

    private fun healthValuesSummary(payload: HealthSyncPayload): String {
        return "Heart ${payload.heartRate?.let { "$it BPM" } ?: "none"}, " +
                "BP ${formatBloodPressure(payload.systolic, payload.diastolic)}, " +
                "Temp ${payload.temperature?.let { String.format(java.util.Locale.US, "%.2f °C", it) } ?: "none"}"
    }

    private fun readingsSummary(readings: LatestReadingsResponse): String {
        return "Dashboard now shows: " +
                "Heart ${readings.heartRate?.value?.let { "$it BPM" } ?: "none"}, " +
                "BP ${readings.bloodPressure?.let { "${it.systolic}/${it.diastolic}" } ?: "none"}, " +
                "Temp ${readings.temperature?.value?.let { String.format(java.util.Locale.US, "%.2f °C", it) } ?: "none"}"
    }

    private fun formatBloodPressure(systolic: Int?, diastolic: Int?): String {
        return if (systolic == null || diastolic == null) {
            "none"
        } else {
            "$systolic/$diastolic"
        }
    }

    private fun appendTrendPoint(
        currentPoints: List<VitalTrendPoint>,
        readings: LatestReadingsResponse
    ): List<VitalTrendPoint> {
        val nextPoint = VitalTrendPoint(
            heartRate = readings.heartRate?.value,
            systolic = readings.bloodPressure?.systolic,
            temperature = readings.temperature?.value
        )

        if (nextPoint.heartRate == null && nextPoint.systolic == null && nextPoint.temperature == null) {
            return currentPoints
        }

        return (currentPoints + nextPoint).takeLast(MAX_TREND_POINTS)
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
        const val LIVE_SYNC_INTERVAL_MILLIS = 5_000L
        const val DEMO_SYNC_INTERVAL_MILLIS = 5_000L
        const val MAX_TREND_POINTS = 12
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
