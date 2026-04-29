package za.ac.tut.healthmonitor.mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import za.ac.tut.healthmonitor.mobile.data.model.LatestReadingsResponse
import za.ac.tut.healthmonitor.mobile.insights.HealthInsight
import za.ac.tut.healthmonitor.mobile.insights.HealthInsightEngine
import za.ac.tut.healthmonitor.mobile.insights.InsightSeverity
import za.ac.tut.healthmonitor.mobile.ui.theme.AccentBlue
import za.ac.tut.healthmonitor.mobile.ui.theme.AccentCoral
import za.ac.tut.healthmonitor.mobile.ui.theme.DeepNavy
import za.ac.tut.healthmonitor.mobile.ui.theme.SoftPanel

@Composable
fun AppScreen(
    uiState: AppUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onShowLogin: () -> Unit,
    onShowSignup: () -> Unit,
    onShowHowItWorks: () -> Unit,
    onSignupTitleChanged: (String) -> Unit,
    onSignupFirstNameChanged: (String) -> Unit,
    onSignupSurnameChanged: (String) -> Unit,
    onSignupDobChanged: (String) -> Unit,
    onSignupGenderChanged: (String) -> Unit,
    onSignupMaritalStatusChanged: (String) -> Unit,
    onSignupEmailChanged: (String) -> Unit,
    onSignupCellNumberChanged: (String) -> Unit,
    onSignupAddressChanged: (String) -> Unit,
    onSignupPasswordChanged: (String) -> Unit,
    onSignupConfirmPasswordChanged: (String) -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onRefresh: () -> Unit,
    onSyncHealthConnect: () -> Unit,
    onSyncSample: () -> Unit,
    onLogout: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DeepNavy, AccentBlue)
                    )
                )
        ) {
            if (uiState.isLoggedIn) {
                DashboardContent(
                    uiState = uiState,
                    onRefresh = onRefresh,
                    onSyncHealthConnect = onSyncHealthConnect,
                    onSyncSample = onSyncSample,
                    onLogout = onLogout
                )
            } else {
                when (uiState.authScreen) {
                    AuthScreen.Login -> LoginContent(
                        uiState = uiState,
                        onEmailChanged = onEmailChanged,
                        onPasswordChanged = onPasswordChanged,
                        onLogin = onLogin,
                        onShowSignup = onShowSignup,
                        onShowHowItWorks = onShowHowItWorks
                    )

                    AuthScreen.Signup -> SignupContent(
                        uiState = uiState,
                        onTitleChanged = onSignupTitleChanged,
                        onFirstNameChanged = onSignupFirstNameChanged,
                        onSurnameChanged = onSignupSurnameChanged,
                        onDobChanged = onSignupDobChanged,
                        onGenderChanged = onSignupGenderChanged,
                        onMaritalStatusChanged = onSignupMaritalStatusChanged,
                        onEmailChanged = onSignupEmailChanged,
                        onCellNumberChanged = onSignupCellNumberChanged,
                        onAddressChanged = onSignupAddressChanged,
                        onPasswordChanged = onSignupPasswordChanged,
                        onConfirmPasswordChanged = onSignupConfirmPasswordChanged,
                        onRegister = onRegister,
                        onShowLogin = onShowLogin,
                        onShowHowItWorks = onShowHowItWorks
                    )

                    AuthScreen.HowItWorks -> HowItWorksContent(
                        onShowLogin = onShowLogin,
                        onShowSignup = onShowSignup
                    )
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun LoginContent(
    uiState: AppUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLogin: () -> Unit,
    onShowSignup: () -> Unit,
    onShowHowItWorks: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SmartHealth Mobile",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sync Galaxy Watch data through your phone and view wellness suggestions.",
            color = Color(0xFFCFEBDD),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = SoftPanel),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = onEmailChanged,
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChanged,
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text("Sign In")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onShowSignup, modifier = Modifier.fillMaxWidth()) {
                    Text("Create Account")
                }
                TextButton(onClick = onShowHowItWorks, modifier = Modifier.fillMaxWidth()) {
                    Text("How The Watch Connection Works")
                }
                MessageSection(uiState)
            }
        }
    }
}

@Composable
private fun SignupContent(
    uiState: AppUiState,
    onTitleChanged: (String) -> Unit,
    onFirstNameChanged: (String) -> Unit,
    onSurnameChanged: (String) -> Unit,
    onDobChanged: (String) -> Unit,
    onGenderChanged: (String) -> Unit,
    onMaritalStatusChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onCellNumberChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onRegister: () -> Unit,
    onShowLogin: () -> Unit,
    onShowHowItWorks: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Create Patient Account",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
        Text(
            text = "Use the same account on the website and this mobile app.",
            color = Color(0xFFCFEBDD),
            style = MaterialTheme.typography.bodyLarge
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = SoftPanel),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AccountField("Title", uiState.signupTitle, onTitleChanged)
                AccountField("First Name *", uiState.signupFirstName, onFirstNameChanged)
                AccountField("Surname *", uiState.signupSurname, onSurnameChanged)
                AccountField("Date of Birth *", uiState.signupDob, onDobChanged, "YYYY-MM-DD")
                AccountField("Gender", uiState.signupGender, onGenderChanged)
                AccountField("Marital Status", uiState.signupMaritalStatus, onMaritalStatusChanged)
                AccountField("Email *", uiState.signupEmail, onEmailChanged, keyboardType = KeyboardType.Email)
                AccountField("Cell Number", uiState.signupCellNumber, onCellNumberChanged, keyboardType = KeyboardType.Phone)
                AccountField("Address", uiState.signupAddress, onAddressChanged)
                AccountField(
                    label = "Password *",
                    value = uiState.signupPassword,
                    onValueChanged = onPasswordChanged,
                    keyboardType = KeyboardType.Password,
                    isPassword = true
                )
                AccountField(
                    label = "Confirm Password *",
                    value = uiState.signupConfirmPassword,
                    onValueChanged = onConfirmPasswordChanged,
                    keyboardType = KeyboardType.Password,
                    isPassword = true
                )

                Button(
                    onClick = onRegister,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text("Create Account")
                }
                TextButton(onClick = onShowLogin, modifier = Modifier.fillMaxWidth()) {
                    Text("Back To Login")
                }
                TextButton(onClick = onShowHowItWorks, modifier = Modifier.fillMaxWidth()) {
                    Text("How This Works")
                }
                MessageSection(uiState)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun AccountField(
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = !label.equals("Address", ignoreCase = true),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun HowItWorksContent(
    onShowLogin: () -> Unit,
    onShowSignup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "How Everything Works",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
        Text(
            text = "The phone app connects approved Galaxy Watch 5 readings to your SmartHealth account.",
            color = Color(0xFFCFEBDD),
            style = MaterialTheme.typography.bodyLarge
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = SoftPanel),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ExplainerStep("1", "Galaxy Watch 5 records health readings such as heart rate.")
                ExplainerStep("2", "Samsung Health receives the watch data on the phone.")
                ExplainerStep("3", "Health Connect lets this app read approved health data.")
                ExplainerStep("4", "The app saves synced readings to the secure SmartHealth service.")
                ExplainerStep("5", "Doctor/staff pages can review patients, while the app shows suggestions only.")
                ExplainerStep("6", "Users can remove Health Connect access from phone settings whenever they want.")
                Text(
                    text = "If no watch or Health Connect data is available, the dashboard will say nothing is connected yet.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "AI suggestions are educational screening prompts, not diagnoses or medical advice.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentCoral
                )
                Button(onClick = onShowLogin, modifier = Modifier.fillMaxWidth()) {
                    Text("Back To Login")
                }
                TextButton(onClick = onShowSignup, modifier = Modifier.fillMaxWidth()) {
                    Text("Create Account")
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ExplainerStep(number: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(34.dp)
                .height(34.dp)
                .background(AccentBlue, RoundedCornerShape(999.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DashboardContent(
    uiState: AppUiState,
    onRefresh: () -> Unit,
    onSyncHealthConnect: () -> Unit,
    onSyncSample: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Phone Bridge Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
        Text(
            text = uiState.userProfile?.let { "${it.firstName} ${it.surname}" } ?: "Logged in",
            color = Color(0xFFCFEBDD),
            style = MaterialTheme.typography.bodyLarge
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = SoftPanel),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Sync Actions", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onSyncHealthConnect, modifier = Modifier.fillMaxWidth()) {
                    Text("Sync From Health Connect")
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                    Text("Refresh")
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = onSyncSample, modifier = Modifier.fillMaxWidth()) {
                    Text("Send Sample Reading")
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                    Text("Logout")
                }
                MessageSection(uiState)
            }
        }

        WatchConnectionCard(uiState.latestReadings)
        VitalCards(uiState.latestReadings)
        AiSuggestionsCard(uiState.latestReadings)
    }
}

@Composable
private fun WatchConnectionCard(readings: LatestReadingsResponse?) {
    val connected = HealthInsightEngine.hasConnectedWatchData(readings)
    val hasAnyReading = HealthInsightEngine.hasAnyReading(readings)
    val statusText = when {
        connected -> "Galaxy Watch / Health Connect data detected."
        hasAnyReading -> "No connected watch detected yet. Showing sample or manual readings."
        else -> "Nothing is connected yet."
    }
    val helperText = if (connected) {
        "Latest readings include Health Connect data. Keep Samsung Health and Health Connect permissions enabled."
    } else {
        "Pair the Galaxy Watch 5 with Samsung Health, allow Samsung Health to share data with Health Connect, then tap Sync From Health Connect."
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SoftPanel),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(6.dp)
                    .background(
                        if (connected) Color(0xFF3CCB9A) else AccentCoral,
                        RoundedCornerShape(999.dp)
                    )
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text("Device Connection", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(statusText, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(helperText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun VitalCards(readings: LatestReadingsResponse?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        VitalCard(
            title = "Heart Rate",
            value = readings?.heartRate?.value?.let { "$it BPM" } ?: "--",
            tint = AccentCoral,
            modifier = Modifier.weight(1f)
        )
        VitalCard(
            title = "Temperature",
            value = readings?.temperature?.value?.let { "$it °C" } ?: "--",
            tint = AccentBlue,
            modifier = Modifier.weight(1f)
        )
    }

    VitalCard(
        title = "Blood Pressure",
        value = readings?.bloodPressure?.let { "${it.systolic}/${it.diastolic} mmHg" } ?: "--",
        tint = Color(0xFF3CCB9A),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun VitalCard(
    title: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SoftPanel),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(6.dp)
                    .background(tint, RoundedCornerShape(999.dp))
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun AiSuggestionsCard(readings: LatestReadingsResponse?) {
    val insights = HealthInsightEngine.buildInsights(readings)

    Card(
        colors = CardDefaults.cardColors(containerColor = SoftPanel),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("AI Suggestions", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Suggestions only. This app does not diagnose disease or replace medical advice.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4E5F7A)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Screens for possible hypertension, hypotension, fever or infection patterns, hypothermia, tachycardia, and bradycardia indicators.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF4E5F7A)
            )
            Spacer(modifier = Modifier.height(14.dp))
            insights.forEachIndexed { index, insight ->
                InsightRow(insight)
                if (index < insights.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun InsightRow(insight: HealthInsight) {
    val tint = when (insight.severity) {
        InsightSeverity.Info -> AccentBlue
        InsightSeverity.Watch -> Color(0xFFF0A83A)
        InsightSeverity.Urgent -> AccentCoral
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .height(10.dp)
                    .background(tint, RoundedCornerShape(999.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(insight.title, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(insight.possibleConcern, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(insight.suggestion, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MessageSection(uiState: AppUiState) {
    uiState.errorMessage?.let {
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = it, color = MaterialTheme.colorScheme.error)
    }
    uiState.infoMessage?.let {
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = it, color = Color(0xFF1E8E5A))
    }
}
