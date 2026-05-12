package za.ac.tut.healthmonitor.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.DisposableEffect
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import za.ac.tut.healthmonitor.mobile.health.HealthConnectManager
import za.ac.tut.healthmonitor.mobile.health.SamsungHealthDataManager
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
            val healthManager = remember { HealthConnectManager(applicationContext) }
            val samsungHealthDataManager = remember { SamsungHealthDataManager(applicationContext) }
            var afterPermissionGranted by remember { mutableStateOf<(() -> Unit)?>(null) }
            var lastSamsungAutoSyncAt by remember { mutableStateOf(0L) }

            val autoSyncSamsungHealthIfAllowed = {
                coroutineScope.launch {
                    val now = System.currentTimeMillis()
                    if (uiState.isLoggedIn && now - lastSamsungAutoSyncAt > AUTO_SYNC_COOLDOWN_MILLIS) {
                        try {
                            if (samsungHealthDataManager.hasHeartRatePermission()) {
                                lastSamsungAutoSyncAt = now
                                appViewModel.syncSamsungHealthSection(samsungHealthDataManager)
                            }
                        } catch (_: Exception) {
                            // Manual sync still shows actionable Samsung Health setup errors.
                        }
                    }
                }
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                PermissionController.createRequestPermissionResultContract()
            ) { grantedPermissions ->
                if (grantedPermissions.any { it in healthManager.requiredPermissions }) {
                    val action = afterPermissionGranted
                    afterPermissionGranted = null
                    if (action == null) {
                        appViewModel.syncLatestSection(healthManager)
                    } else {
                        action()
                    }
                } else {
                    afterPermissionGranted = null
                    appViewModel.setInfoMessage("Health Connect permissions were not granted.")
                }
            }

            LaunchedEffect(Unit) {
                appViewModel.clearMessages()
            }

            LaunchedEffect(uiState.isLoggedIn) {
                autoSyncSamsungHealthIfAllowed()
                while (uiState.isLoggedIn) {
                    delay(FOREGROUND_SYNC_INTERVAL_MILLIS)
                    autoSyncSamsungHealthIfAllowed()
                }
            }

            DisposableEffect(Unit) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_START) {
                        autoSyncSamsungHealthIfAllowed()
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
                    onRefresh = appViewModel::refreshDashboard,
                    onSyncLatestSection = {
                        coroutineScope.launch {
                            when (healthManager.availabilityStatus()) {
                                HealthConnectClient.SDK_UNAVAILABLE -> {
                                    appViewModel.setInfoMessage("Health Connect is not available on this phone.")
                                }

                                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                                    openHealthConnectInPlayStore()
                                }

                                else -> {
                                    if (healthManager.hasAnyPermission()) {
                                        appViewModel.syncLatestSection(healthManager)
                                    } else {
                                        afterPermissionGranted = {
                                            appViewModel.syncLatestSection(healthManager)
                                        }
                                        permissionLauncher.launch(healthManager.requiredPermissions)
                                    }
                                }
                            }
                        }
                    },
                    onOpenHealthConnect = ::openHealthConnectSettings,
                    onOpenSamsungHealth = {
                        if (!openInstalledApp(SAMSUNG_HEALTH_PACKAGE)) {
                            openPlayStore(SAMSUNG_HEALTH_PACKAGE)
                        }
                    },
                    onSyncSamsungHealth = {
                        coroutineScope.launch {
                            try {
                                if (samsungHealthDataManager.requestHeartRatePermission(this@MainActivity)) {
                                    appViewModel.syncSamsungHealthSection(samsungHealthDataManager)
                                } else {
                                    appViewModel.setInfoMessage("Samsung Health heart-rate permission was not granted.")
                                }
                            } catch (e: Exception) {
                                if (!samsungHealthDataManager.resolveIfPossible(e, this@MainActivity)) {
                                    appViewModel.setInfoMessage(samsungHealthDataManager.toUserMessage(e))
                                }
                            }
                        }
                    },
                    onStartDemoSync = appViewModel::startDemoSync,
                    onStartEmergencyDemoSync = appViewModel::startEmergencyDemoSync,
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
                    onSendParamedicAlert = appViewModel::sendParamedicAlert
                )
            }
        }
    }

    private fun openHealthConnectInPlayStore() {
        val uriString = "market://details?id=$HEALTH_CONNECT_PACKAGE&url=healthconnect%3A%2F%2Fonboarding"
        startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setPackage("com.android.vending")
                data = Uri.parse(uriString)
                putExtra("overlay", true)
                putExtra("callerId", packageName)
            }
        )
    }

    private fun openHealthConnectSettings() {
        val settingsIntent = Intent(HEALTH_CONNECT_SETTINGS_ACTION)
        if (settingsIntent.resolveActivity(packageManager) != null) {
            startActivity(settingsIntent)
            return
        }

        if (!openInstalledApp(HEALTH_CONNECT_PACKAGE)) {
            openHealthConnectInPlayStore()
        }
    }

    private fun openInstalledApp(packageName: String): Boolean {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return false
        startActivity(launchIntent)
        return true
    }

    private fun openPlayStore(packageName: String) {
        startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
            }
        )
    }

    private companion object {
        const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"
        const val SAMSUNG_HEALTH_PACKAGE = "com.sec.android.app.shealth"
        const val HEALTH_CONNECT_SETTINGS_ACTION = "android.health.connect.action.HEALTH_CONNECT_SETTINGS"
        const val FOREGROUND_SYNC_INTERVAL_MILLIS = 2L * 60L * 1000L
        const val AUTO_SYNC_COOLDOWN_MILLIS = FOREGROUND_SYNC_INTERVAL_MILLIS
    }
}
