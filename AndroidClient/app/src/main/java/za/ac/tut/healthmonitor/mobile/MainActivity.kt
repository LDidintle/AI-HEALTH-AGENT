package za.ac.tut.healthmonitor.mobile

import android.Manifest
import android.os.Bundle
import android.os.Build
import androidx.compose.runtime.DisposableEffect
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import za.ac.tut.healthmonitor.mobile.health.SamsungHealthDataManager
import za.ac.tut.healthmonitor.mobile.monitoring.WatchAlertMonitoringService
import za.ac.tut.healthmonitor.mobile.ui.AppScreen
import za.ac.tut.healthmonitor.mobile.ui.AppViewModel
import za.ac.tut.healthmonitor.mobile.ui.theme.HealthMonitorTheme
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val appViewModel: AppViewModel = viewModel()
            val uiState by appViewModel.uiState.collectAsState()
            val coroutineScope = rememberCoroutineScope()
            val samsungHealthDataManager = remember { SamsungHealthDataManager(applicationContext) }
            var lastSamsungAutoSyncAt by remember { mutableStateOf(0L) }
            var samsungPermissionPrompted by remember { mutableStateOf(false) }
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { }

            fun requestNotificationPermissionIfNeeded() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            fun autoSyncSamsungHealthIfAllowed(
                force: Boolean = false,
                allowPermissionPrompt: Boolean = true
            ) {
                coroutineScope.launch {
                    val now = System.currentTimeMillis()
                    if (uiState.isLoggedIn && (force || now - lastSamsungAutoSyncAt > AUTO_SYNC_COOLDOWN_MILLIS)) {
                        try {
                            val hasPermission = samsungHealthDataManager.hasAnyRequiredPermission() ||
                                (allowPermissionPrompt && !samsungPermissionPrompted &&
                                    samsungHealthDataManager.requestRequiredPermissions(this@MainActivity))
                            if (allowPermissionPrompt) {
                                samsungPermissionPrompted = true
                            }
                            if (hasPermission) {
                                lastSamsungAutoSyncAt = now
                                appViewModel.syncSamsungHealthSection(samsungHealthDataManager)
                            }
                        } catch (_: Exception) {
                            // Keep automatic sync quiet; the dashboard explains missing watch data.
                        }
                    }
                }
            }

            LaunchedEffect(Unit) {
                appViewModel.clearMessages()
            }

            LaunchedEffect(uiState.isLoggedIn) {
                if (uiState.isLoggedIn) {
                    requestNotificationPermissionIfNeeded()
                    WatchAlertMonitoringService.start(applicationContext)
                    autoSyncSamsungHealthIfAllowed(force = true)
                    while (uiState.isLoggedIn) {
                        delay(FOREGROUND_SYNC_INTERVAL_MILLIS)
                        autoSyncSamsungHealthIfAllowed()
                    }
                } else {
                    WatchAlertMonitoringService.stop(applicationContext)
                }
            }

            DisposableEffect(Unit) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_START,
                        Lifecycle.Event.ON_RESUME -> autoSyncSamsungHealthIfAllowed(force = true)
                        Lifecycle.Event.ON_PAUSE -> autoSyncSamsungHealthIfAllowed(
                            force = true,
                            allowPermissionPrompt = false
                        )
                        else -> Unit
                    }
                }
                lifecycle.addObserver(observer)
                onDispose { lifecycle.removeObserver(observer) }
            }

            HealthMonitorTheme {
                AppScreen(
                    uiState = uiState,
                    onEmailChanged = appViewModel::updateEmail,
                    onPasswordChanged = appViewModel::updatePassword,
                    onShowLogin = appViewModel::showLogin,
                    onShowSignup = appViewModel::showSignup,
                    onShowHowItWorks = appViewModel::showHowItWorks,
                    onSignupFirstNameChanged = appViewModel::updateSignupFirstName,
                    onSignupSurnameChanged = appViewModel::updateSignupSurname,
                    onSignupEmailChanged = appViewModel::updateSignupEmail,
                    onSignupDobChanged = appViewModel::updateSignupDob,
                    onSignupPasswordChanged = appViewModel::updateSignupPassword,
                    onSignupConfirmPasswordChanged = appViewModel::updateSignupConfirmPassword,
                    onLogin = appViewModel::login,
                    onRegister = appViewModel::register,
                    onLogout = appViewModel::logout,
                    onSelectLanguage = appViewModel::selectLanguage,
                    onOpenProfile = appViewModel::openProfile,
                    onCloseProfile = appViewModel::closeProfile,
                    onEditTitleChanged = appViewModel::updateEditTitle,
                    onEditFirstNameChanged = appViewModel::updateEditFirstName,
                    onEditSurnameChanged = appViewModel::updateEditSurname,
                    onEditDobChanged = appViewModel::updateEditDob,
                    onEditGenderChanged = appViewModel::updateEditGender,
                    onEditMaritalStatusChanged = appViewModel::updateEditMaritalStatus,
                    onEditCellNumberChanged = appViewModel::updateEditCellNumber,
                    onEditIdNumberChanged = appViewModel::updateEditIdNumber,
                    onEditEmergencyContactNameChanged = appViewModel::updateEditEmergencyContactName,
                    onEditEmergencyContactNumberChanged = appViewModel::updateEditEmergencyContactNumber,
                    onEditBloodGroupChanged = appViewModel::updateEditBloodGroup,
                    onEditKnownAllergiesChanged = appViewModel::updateEditKnownAllergies,
                    onEditChronicConditionsChanged = appViewModel::updateEditChronicConditions,
                    onEditAddressChanged = appViewModel::updateEditAddress,
                    onSaveProfile = appViewModel::saveProfile,
                    onOpenChat = appViewModel::openChat,
                    onCloseChat = appViewModel::closeChat,
                    onChatInputChanged = appViewModel::updateChatInput,
                    onSendChatMessage = appViewModel::sendChatMessage,
                    onSendParamedicAlert = appViewModel::sendParamedicAlert,
                    onReadingContextChanged = appViewModel::selectReadingContext,
                    onSleepStartChanged = appViewModel::updateSleepStart,
                    onSleepEndChanged = appViewModel::updateSleepEnd
                )
            }
        }
    }

    private companion object {
        const val FOREGROUND_SYNC_INTERVAL_MILLIS = 8L * 1000L
        const val AUTO_SYNC_COOLDOWN_MILLIS = FOREGROUND_SYNC_INTERVAL_MILLIS
    }
}
