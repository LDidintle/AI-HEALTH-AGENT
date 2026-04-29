package za.ac.tut.healthmonitor.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import kotlinx.coroutines.launch
import za.ac.tut.healthmonitor.mobile.health.HealthConnectManager
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

            val permissionLauncher = rememberLauncherForActivityResult(
                PermissionController.createRequestPermissionResultContract()
            ) { grantedPermissions ->
                if (grantedPermissions.containsAll(healthManager.requiredPermissions)) {
                    appViewModel.syncFromHealthConnect(healthManager)
                } else {
                    appViewModel.setInfoMessage("Health Connect permissions were not granted.")
                }
            }

            LaunchedEffect(Unit) {
                appViewModel.clearMessages()
            }

            HealthMonitorTheme {
                AppScreen(
                    uiState = uiState,
                    onEmailChanged = appViewModel::updateEmail,
                    onPasswordChanged = appViewModel::updatePassword,
                    onShowLogin = appViewModel::showLogin,
                    onShowSignup = appViewModel::showSignup,
                    onShowHowItWorks = appViewModel::showHowItWorks,
                    onSignupTitleChanged = appViewModel::updateSignupTitle,
                    onSignupFirstNameChanged = appViewModel::updateSignupFirstName,
                    onSignupSurnameChanged = appViewModel::updateSignupSurname,
                    onSignupDobChanged = appViewModel::updateSignupDob,
                    onSignupGenderChanged = appViewModel::updateSignupGender,
                    onSignupMaritalStatusChanged = appViewModel::updateSignupMaritalStatus,
                    onSignupEmailChanged = appViewModel::updateSignupEmail,
                    onSignupCellNumberChanged = appViewModel::updateSignupCellNumber,
                    onSignupAddressChanged = appViewModel::updateSignupAddress,
                    onSignupPasswordChanged = appViewModel::updateSignupPassword,
                    onSignupConfirmPasswordChanged = appViewModel::updateSignupConfirmPassword,
                    onLogin = appViewModel::login,
                    onRegister = appViewModel::register,
                    onRefresh = appViewModel::refreshDashboard,
                    onSyncHealthConnect = {
                        coroutineScope.launch {
                            when (healthManager.availabilityStatus()) {
                                HealthConnectClient.SDK_UNAVAILABLE -> {
                                    appViewModel.setInfoMessage("Health Connect is not available on this phone.")
                                }

                                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                                    openHealthConnectInPlayStore()
                                }

                                else -> {
                                    if (healthManager.hasAllPermissions()) {
                                        appViewModel.syncFromHealthConnect(healthManager)
                                    } else {
                                        permissionLauncher.launch(healthManager.requiredPermissions)
                                    }
                                }
                            }
                        }
                    },
                    onSyncSample = appViewModel::syncManualSample,
                    onLogout = appViewModel::logout,
                    onSelectLanguage = appViewModel::selectLanguage,
                    onOpenProfile = appViewModel::openProfile,
                    onCloseProfile = appViewModel::closeProfile,
                    onEditTitleChanged = appViewModel::updateEditTitle,
                    onEditFirstNameChanged = appViewModel::updateEditFirstName,
                    onEditSurnameChanged = appViewModel::updateEditSurname,
                    onEditGenderChanged = appViewModel::updateEditGender,
                    onEditCellNumberChanged = appViewModel::updateEditCellNumber,
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
        val uriString =
            "market://details?id=com.google.android.apps.healthdata&url=healthconnect%3A%2F%2Fonboarding"
        startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setPackage("com.android.vending")
                data = Uri.parse(uriString)
                putExtra("overlay", true)
                putExtra("callerId", packageName)
            }
        )
    }
}
