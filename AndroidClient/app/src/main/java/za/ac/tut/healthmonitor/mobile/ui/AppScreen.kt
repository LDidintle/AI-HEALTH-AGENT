package za.ac.tut.healthmonitor.mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import za.ac.tut.healthmonitor.mobile.data.model.BloodPressureValue
import za.ac.tut.healthmonitor.mobile.data.model.LatestReadingsResponse
import za.ac.tut.healthmonitor.mobile.data.model.ReadingValue
import za.ac.tut.healthmonitor.mobile.data.model.TemperatureValue
import za.ac.tut.healthmonitor.mobile.insights.HealthInsight
import za.ac.tut.healthmonitor.mobile.insights.HealthInsightEngine
import za.ac.tut.healthmonitor.mobile.insights.InsightSeverity
import za.ac.tut.healthmonitor.mobile.ui.theme.AccentBlue
import za.ac.tut.healthmonitor.mobile.ui.theme.AccentCoral
import za.ac.tut.healthmonitor.mobile.ui.theme.DeepNavy
import za.ac.tut.healthmonitor.mobile.ui.theme.SoftPanel

private val Ink = Color(0xFF101827)
private val Muted = Color(0xFF5A6680)
private val Yellow = Color(0xFFFFDD59)
private val Danger = Color(0xFFA82118)
private val Glass = Color(0x6636465F)

@Composable
fun AppScreen(
    uiState: AppUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onShowLogin: () -> Unit,
    onShowSignup: () -> Unit,
    onShowHowItWorks: () -> Unit,
    onSignupFirstNameChanged: (String) -> Unit,
    onSignupSurnameChanged: (String) -> Unit,
    onSignupEmailChanged: (String) -> Unit,
    onSignupDobChanged: (String) -> Unit,
    onSignupPasswordChanged: (String) -> Unit,
    onSignupConfirmPasswordChanged: (String) -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onRefresh: () -> Unit,
    onSyncLatestSection: () -> Unit,
    onOpenHealthConnect: () -> Unit,
    onOpenSamsungHealth: () -> Unit,
    onSyncSamsungHealth: () -> Unit,
    onStartDemoSync: () -> Unit,
    onStartEmergencyDemoSync: () -> Unit,
    onLogout: () -> Unit,
    onSelectLanguage: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onCloseProfile: () -> Unit,
    onEditTitleChanged: (String) -> Unit,
    onEditFirstNameChanged: (String) -> Unit,
    onEditSurnameChanged: (String) -> Unit,
    onEditDobChanged: (String) -> Unit,
    onEditGenderChanged: (String) -> Unit,
    onEditMaritalStatusChanged: (String) -> Unit,
    onEditCellNumberChanged: (String) -> Unit,
    onEditIdNumberChanged: (String) -> Unit,
    onEditEmergencyContactNameChanged: (String) -> Unit,
    onEditEmergencyContactNumberChanged: (String) -> Unit,
    onEditBloodGroupChanged: (String) -> Unit,
    onEditKnownAllergiesChanged: (String) -> Unit,
    onEditChronicConditionsChanged: (String) -> Unit,
    onEditAddressChanged: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onOpenChat: () -> Unit,
    onCloseChat: () -> Unit,
    onChatInputChanged: (String) -> Unit,
    onSendChatMessage: () -> Unit,
    onSendParamedicAlert: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(DeepNavy, Color(0xFF052415), AccentBlue)))
        ) {
            if (uiState.isLoggedIn) {
                DashboardContent(
                    uiState = uiState,
                    onRefresh = onRefresh,
                    onSyncLatestSection = onSyncLatestSection,
                    onOpenHealthConnect = onOpenHealthConnect,
                    onOpenSamsungHealth = onOpenSamsungHealth,
                    onSyncSamsungHealth = onSyncSamsungHealth,
                    onStartDemoSync = onStartDemoSync,
                    onStartEmergencyDemoSync = onStartEmergencyDemoSync,
                    onLogout = onLogout,
                    onSelectLanguage = onSelectLanguage,
                    onOpenProfile = onOpenProfile,
                    onOpenChat = onOpenChat,
                    onSendParamedicAlert = onSendParamedicAlert
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
                        onFirstNameChanged = onSignupFirstNameChanged,
                        onSurnameChanged = onSignupSurnameChanged,
                        onEmailChanged = onSignupEmailChanged,
                        onDobChanged = onSignupDobChanged,
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

            if (uiState.isProfileOpen) {
                ProfileDialog(
                    uiState = uiState,
                    onClose = onCloseProfile,
                    onTitleChanged = onEditTitleChanged,
                    onFirstNameChanged = onEditFirstNameChanged,
                    onSurnameChanged = onEditSurnameChanged,
                    onDobChanged = onEditDobChanged,
                    onGenderChanged = onEditGenderChanged,
                    onMaritalStatusChanged = onEditMaritalStatusChanged,
                    onCellNumberChanged = onEditCellNumberChanged,
                    onIdNumberChanged = onEditIdNumberChanged,
                    onEmergencyContactNameChanged = onEditEmergencyContactNameChanged,
                    onEmergencyContactNumberChanged = onEditEmergencyContactNumberChanged,
                    onBloodGroupChanged = onEditBloodGroupChanged,
                    onKnownAllergiesChanged = onEditKnownAllergiesChanged,
                    onChronicConditionsChanged = onEditChronicConditionsChanged,
                    onAddressChanged = onEditAddressChanged,
                    onSaveProfile = onSaveProfile
                )
            }

            if (uiState.isChatOpen) {
                ChatDialog(
                    uiState = uiState,
                    onClose = onCloseChat,
                    onChatInputChanged = onChatInputChanged,
                    onSendChatMessage = onSendChatMessage
                )
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
        Text("SmartHealth Mobile", style = MaterialTheme.typography.headlineLarge, color = Yellow, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sync Galaxy Watch data through your phone and view wellness suggestions.",
            color = Color(0xFFCFEBDD),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(colors = CardDefaults.cardColors(containerColor = SoftPanel), shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                OutlinedTextField(uiState.email, onEmailChanged, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
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
                Button(onClick = onLogin, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 14.dp)) {
                    Text("Sign In")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onShowSignup, modifier = Modifier.fillMaxWidth()) { Text("Create Account") }
                TextButton(onClick = onShowHowItWorks, modifier = Modifier.fillMaxWidth()) { Text("How The Watch Connection Works") }
                MessageSection(uiState)
            }
        }
    }
}

@Composable
private fun SignupContent(
    uiState: AppUiState,
    onFirstNameChanged: (String) -> Unit,
    onSurnameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onDobChanged: (String) -> Unit,
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
        Text("Create Patient Account", style = MaterialTheme.typography.headlineMedium, color = Yellow, fontWeight = FontWeight.Bold)
        Text("Create your login first. Medical and emergency details can be completed after sign in.", color = Color(0xFFCFEBDD), style = MaterialTheme.typography.bodyLarge)

        Card(colors = CardDefaults.cardColors(containerColor = SoftPanel), shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AccountField("First Name *", uiState.signupFirstName, onFirstNameChanged)
                AccountField("Surname *", uiState.signupSurname, onSurnameChanged)
                AccountField("Email *", uiState.signupEmail, onEmailChanged, keyboardType = KeyboardType.Email)
                AccountField("Date of Birth *", uiState.signupDob, onDobChanged, placeholder = "YYYY-MM-DD")
                AccountField("Password *", uiState.signupPassword, onPasswordChanged, keyboardType = KeyboardType.Password, isPassword = true)
                AccountField("Confirm Password *", uiState.signupConfirmPassword, onConfirmPasswordChanged, keyboardType = KeyboardType.Password, isPassword = true)
                Button(onClick = onRegister, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 14.dp)) { Text("Create Account") }
                TextButton(onClick = onShowLogin, modifier = Modifier.fillMaxWidth()) { Text("Back To Login") }
                TextButton(onClick = onShowHowItWorks, modifier = Modifier.fillMaxWidth()) { Text("How This Works") }
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
private fun HowItWorksContent(onShowLogin: () -> Unit, onShowSignup: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(28.dp))
        Text("How Everything Works", style = MaterialTheme.typography.headlineMedium, color = Yellow, fontWeight = FontWeight.Bold)
        Text("The phone app connects approved Galaxy Watch 5 readings to your SmartHealth account.", color = Color(0xFFCFEBDD))
        Card(colors = CardDefaults.cardColors(containerColor = SoftPanel), shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ExplainerStep("1", "Galaxy Watch 5 records health readings such as heart rate.")
                ExplainerStep("2", "Samsung Health receives the watch data on the phone.")
                ExplainerStep("3", "Health Connect lets this app read approved health data.")
                ExplainerStep("4", "The app saves synced readings to the secure SmartHealth service.")
                ExplainerStep("5", "Doctor/staff pages can review patients, while the app shows suggestions only.")
                Text("AI suggestions are educational screening prompts, not diagnoses or medical advice.", color = AccentCoral)
                Button(onClick = onShowLogin, modifier = Modifier.fillMaxWidth()) { Text("Back To Login") }
                TextButton(onClick = onShowSignup, modifier = Modifier.fillMaxWidth()) { Text("Create Account") }
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
                .size(34.dp)
                .background(AccentBlue, CircleShape),
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
    onSyncLatestSection: () -> Unit,
    onOpenHealthConnect: () -> Unit,
    onOpenSamsungHealth: () -> Unit,
    onSyncSamsungHealth: () -> Unit,
    onStartDemoSync: () -> Unit,
    onStartEmergencyDemoSync: () -> Unit,
    onLogout: () -> Unit,
    onSelectLanguage: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenChat: () -> Unit,
    onSendParamedicAlert: () -> Unit
) {
    val copy = copyFor(uiState.selectedLanguage)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Patient Dashboard", style = MaterialTheme.typography.headlineLarge, color = Yellow, fontWeight = FontWeight.Bold)
                Text(uiState.userProfile?.let { "${it.firstName} ${it.surname}" } ?: "Logged in", color = Color(0xFFCFEBDD))
            }
            ProfileButton(onClick = onOpenProfile)
        }

        LanguageTabs(uiState.selectedLanguage, onSelectLanguage)
        MessageSection(uiState)

        VitalsPanel(uiState.latestReadings, copy)
        ChartCard(uiState.trendPoints)
        ActionButton(text = copy.chat, onClick = onOpenChat, colors = listOf(AccentBlue, Yellow), textColor = Ink)
        ActionButton(text = copy.alert, onClick = onSendParamedicAlert, colors = listOf(Danger, Color(0xFF8F1B13)), textColor = Color.White)

        EmergencyNotificationCard(uiState)

        SyncActionsCard(
            uiState = uiState,
            onRefresh = onRefresh,
            onSyncLatestSection = onSyncLatestSection,
            onOpenHealthConnect = onOpenHealthConnect,
            onOpenSamsungHealth = onOpenSamsungHealth,
            onSyncSamsungHealth = onSyncSamsungHealth,
            onStartDemoSync = onStartDemoSync,
            onStartEmergencyDemoSync = onStartEmergencyDemoSync,
            onLogout = onLogout
        )
        AiSuggestionsCard(uiState.latestReadings, copy)
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ProfileButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .background(Color(0xFF142034), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(AccentBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("P", color = Ink, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LanguageTabs(selected: String, onSelectLanguage: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Glass, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LanguageTab("en", "English", selected, onSelectLanguage, Modifier.weight(1f))
        LanguageTab("zu", "IsiZulu", selected, onSelectLanguage, Modifier.weight(1f))
        LanguageTab("af", "Afrikaans", selected, onSelectLanguage, Modifier.weight(1f))
    }
}

@Composable
private fun LanguageTab(code: String, label: String, selected: String, onSelectLanguage: (String) -> Unit, modifier: Modifier) {
    val active = selected == code
    Box(
        modifier = modifier
            .background(if (active) AccentBlue else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable { onSelectLanguage(code) }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (active) Ink else Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun VitalsPanel(readings: LatestReadingsResponse?, copy: DashboardCopy) {
    Card(colors = CardDefaults.cardColors(containerColor = SoftPanel), shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            VitalRow(
                label = copy.heart,
                value = readings?.heartRate?.value?.let { "$it BPM" } ?: "--",
                detail = readings?.heartRate?.detailText(),
                tint = AccentCoral
            )
            DividerLine()
            VitalRow(
                label = copy.blood,
                value = readings?.bloodPressure?.let { "${it.systolic}/${it.diastolic} mmHg" } ?: "--",
                detail = readings?.bloodPressure?.detailText(),
                tint = AccentBlue
            )
            DividerLine()
            VitalRow(
                label = copy.temp,
                value = readings?.temperature?.value?.let { "$it °C" } ?: "--",
                detail = readings?.temperature?.detailText(),
                tint = Color(0xFFFF8A5C)
            )
        }
    }
}

@Composable
private fun VitalRow(label: String, value: String, detail: String?, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(tint.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(16.dp).background(tint, CircleShape))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.titleLarge, color = Ink)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = tint, fontWeight = FontWeight.Bold)
            detail?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(it, color = Muted, fontSize = 12.sp)
            }
        }
    }
}

private fun ReadingValue.detailText(): String? {
    return readingDetail(source, recordedAt)
}

private fun TemperatureValue.detailText(): String? {
    return readingDetail(source, recordedAt)
}

private fun BloodPressureValue.detailText(): String? {
    return readingDetail(source, recordedAt)
}

private fun readingDetail(source: String?, recordedAt: String?): String? {
    val parts = listOfNotNull(
        source?.takeIf { it.isNotBlank() },
        recordedAt?.takeIf { it.isNotBlank() }?.let { "at $it" }
    )
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" ")
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFD8DEE8))
    )
}

@Composable
private fun ChartCard(trendPoints: List<VitalTrendPoint>) {
    Card(colors = CardDefaults.cardColors(containerColor = SoftPanel), shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Section trend graph", color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                Text("• Heart", color = Color(0xFFB42318), fontWeight = FontWeight.Bold)
                Text("• BP", color = AccentBlue, fontWeight = FontWeight.Bold)
                Text("• Temp", color = AccentCoral, fontWeight = FontWeight.Bold)
            }

            if (trendPoints.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color(0xFFEFF6F4), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sync latest section to draw trends", color = Muted)
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFFEFF6F4), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    val gridColor = Color(0xFFD7DEEA)
                    repeat(5) { index ->
                        val y = size.height * index / 4f
                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.5f)
                    }
                    repeat(4) { index ->
                        val x = size.width * index / 3f
                        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.5f)
                    }

                    drawTrendLine(trendPoints.mapNotNull { it.heartRate?.toFloat() }, Color(0xFFB42318))
                    drawTrendLine(trendPoints.mapNotNull { it.systolic?.toFloat() }, AccentBlue)
                    drawTrendLine(trendPoints.mapNotNull { it.temperature?.toFloat() }, AccentCoral)
                }
            }
        }
    }
}

private fun DrawScope.drawTrendLine(values: List<Float>, color: Color) {
    if (values.size < 2) return

    val min = values.minOrNull() ?: return
    val max = values.maxOrNull() ?: return
    val range = (max - min).takeIf { it > 0.01f } ?: 1f
    val stepX = size.width / (values.size - 1)
    val path = Path()

    values.forEachIndexed { index, value ->
        val x = stepX * index
        val y = size.height - ((value - min) / range) * size.height
        if (index == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 5f, cap = StrokeCap.Round)
    )

    values.forEachIndexed { index, value ->
        val x = stepX * index
        val y = size.height - ((value - min) / range) * size.height
        drawCircle(color = color, radius = 5f, center = Offset(x, y))
    }
}

@Composable
private fun ActionButton(text: String, onClick: () -> Unit, colors: List<Color>, textColor: Color) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(vertical = 18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(colors), RoundedCornerShape(16.dp))
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = textColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun EmergencyNotificationCard(uiState: AppUiState) {
    val alert = uiState.activeAlert
    if (!uiState.alertSent && alert == null) {
        return
    }

    Card(colors = CardDefaults.cardColors(containerColor = Danger), shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Emergency Alert Notification", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(
                text = alert?.message ?: "Emergency alert shown. If this is serious, call emergency services now.",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
            alert?.hospitalName?.takeIf { it.isNotBlank() }?.let {
                Text("Assigned hospital: $it", color = Color.White, style = MaterialTheme.typography.bodyMedium)
            }
            alert?.status?.takeIf { it.isNotBlank() }?.let {
                Text("Status: $it", color = Color.White, style = MaterialTheme.typography.bodyMedium)
            }
            alert?.bpm?.let {
                Text("Heart rate: $it BPM", color = Color.White, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SyncActionsCard(
    uiState: AppUiState,
    onRefresh: () -> Unit,
    onSyncLatestSection: () -> Unit,
    onOpenHealthConnect: () -> Unit,
    onOpenSamsungHealth: () -> Unit,
    onSyncSamsungHealth: () -> Unit,
    onStartDemoSync: () -> Unit,
    onStartEmergencyDemoSync: () -> Unit,
    onLogout: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Glass), shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Sync Actions", color = Color.White, fontWeight = FontWeight.Bold)
            Text(
                text = "Section sync: imports Health Connect first, or Samsung Health directly for testing.",
                color = Color(0xFFCFEBDD),
                style = MaterialTheme.typography.bodyMedium
            )
            HealthConnectChecklist()
            uiState.lastSectionSyncAt?.let {
                Text("Last section sync: $it", color = Color(0xFFCFEBDD), style = MaterialTheme.typography.bodySmall)
            }
            uiState.lastSyncSummary?.let {
                Text(it, color = Color(0xFFCFEBDD), style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = onSyncLatestSection,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sync Health Connect")
            }
            Button(
                onClick = onSyncSamsungHealth,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sync Samsung Health")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onOpenHealthConnect, modifier = Modifier.weight(1f)) {
                    Text("Health Connect")
                }
                TextButton(onClick = onOpenSamsungHealth, modifier = Modifier.weight(1f)) {
                    Text("Samsung Health")
                }
            }
            Button(
                onClick = onStartDemoSync,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Ink)
            ) {
                Text("Load Demo Section")
            }
            Button(
                onClick = onStartEmergencyDemoSync,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Danger, contentColor = Color.White)
            ) {
                Text("Trigger Emergency Demo Alert")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onRefresh, modifier = Modifier.weight(1f)) { Text("Clear") }
                TextButton(onClick = onLogout, modifier = Modifier.weight(1f)) { Text("Logout") }
            }
        }
    }
}

@Composable
private fun HealthConnectChecklist() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("For real watch data:", color = Color.White, fontWeight = FontWeight.Bold)
        Text("1. Samsung Health must write heart rate into Health Connect.", color = Color(0xFFCFEBDD), style = MaterialTheme.typography.bodySmall)
        Text("2. Health Connect > Data and access > Heart rate must show an entry.", color = Color(0xFFCFEBDD), style = MaterialTheme.typography.bodySmall)
        Text("3. Take a fresh heart-rate reading after permissions are enabled.", color = Color(0xFFCFEBDD), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun AiSuggestionsCard(readings: LatestReadingsResponse?, copy: DashboardCopy) {
    val insights = HealthInsightEngine.buildInsights(readings)

    Card(colors = CardDefaults.cardColors(containerColor = SoftPanel), shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(copy.causes, style = MaterialTheme.typography.headlineSmall, color = Ink, fontWeight = FontWeight.Bold)
            DividerLine()
            Spacer(modifier = Modifier.height(16.dp))
            insights.forEachIndexed { index, insight ->
                InsightRow(index + 1, insight)
                if (index < insights.lastIndex) Spacer(modifier = Modifier.height(14.dp))
            }
            if (insights.isEmpty()) {
                Text("No synced data available yet.", color = Ink)
            }
        }
    }
}

@Composable
private fun InsightRow(number: Int, insight: HealthInsight) {
    val tint = when (insight.severity) {
        InsightSeverity.Info -> AccentBlue
        InsightSeverity.Watch -> Color(0xFFF0A83A)
        InsightSeverity.Urgent -> AccentCoral
    }

    Row(verticalAlignment = Alignment.Top) {
        Text("$number.", color = tint, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(insight.title, style = MaterialTheme.typography.titleMedium, color = Ink)
            Spacer(modifier = Modifier.height(4.dp))
            Text(insight.suggestion, style = MaterialTheme.typography.bodyMedium, color = Ink)
        }
    }
}

@Composable
private fun ProfileDialog(
    uiState: AppUiState,
    onClose: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onFirstNameChanged: (String) -> Unit,
    onSurnameChanged: (String) -> Unit,
    onDobChanged: (String) -> Unit,
    onGenderChanged: (String) -> Unit,
    onMaritalStatusChanged: (String) -> Unit,
    onCellNumberChanged: (String) -> Unit,
    onIdNumberChanged: (String) -> Unit,
    onEmergencyContactNameChanged: (String) -> Unit,
    onEmergencyContactNumberChanged: (String) -> Unit,
    onBloodGroupChanged: (String) -> Unit,
    onKnownAllergiesChanged: (String) -> Unit,
    onChronicConditionsChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onSaveProfile: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Complete Your Information") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = uiState.userProfile?.let { "${it.firstName} ${it.surname} - ${it.email}" }.orEmpty(),
                    color = Muted
                )
                AccountField("Title", uiState.editTitle, onTitleChanged)
                AccountField("First Name", uiState.editFirstName, onFirstNameChanged)
                AccountField("Surname", uiState.editSurname, onSurnameChanged)
                AccountField("Date of Birth", uiState.editDob, onDobChanged, placeholder = "YYYY-MM-DD")
                AccountField("Gender", uiState.editGender, onGenderChanged)
                AccountField("Marital Status", uiState.editMaritalStatus, onMaritalStatusChanged)
                AccountField("Cell Number", uiState.editCellNumber, onCellNumberChanged, keyboardType = KeyboardType.Phone)
                AccountField("South African ID Number", uiState.editIdNumber, onIdNumberChanged, keyboardType = KeyboardType.Number)
                AccountField("Emergency Contact / Next of Kin Name", uiState.editEmergencyContactName, onEmergencyContactNameChanged)
                AccountField("Emergency Contact Cell Number", uiState.editEmergencyContactNumber, onEmergencyContactNumberChanged, keyboardType = KeyboardType.Phone)
                AccountField("Blood Group", uiState.editBloodGroup, onBloodGroupChanged)
                AccountField("Known Allergies", uiState.editKnownAllergies, onKnownAllergiesChanged)
                AccountField("Chronic Conditions", uiState.editChronicConditions, onChronicConditionsChanged)
                AccountField("Address", uiState.editAddress, onAddressChanged)
            }
        },
        confirmButton = { Button(onClick = onSaveProfile) { Text("Save and Continue") } },
        dismissButton = { TextButton(onClick = onClose) { Text("Fill In Later") } }
    )
}

@Composable
private fun ChatDialog(
    uiState: AppUiState,
    onClose: () -> Unit,
    onChatInputChanged: (String) -> Unit,
    onSendChatMessage: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("SmartHealth Assistant") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(
                    modifier = Modifier
                        .height(260.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.chatMessages.forEach { message ->
                        ChatBubble(message)
                    }
                }
                OutlinedTextField(
                    value = uiState.chatInput,
                    onValueChange = onChatInputChanged,
                    label = { Text("Type your message") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { Button(onClick = onSendChatMessage) { Text("Send") } },
        dismissButton = { TextButton(onClick = onClose) { Text("Close") } }
    )
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .background(if (message.fromUser) AccentBlue else Color(0xFFEFF5F2), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Text(message.text, color = if (message.fromUser) Ink else Ink)
        }
    }
}

@Composable
private fun MessageSection(uiState: AppUiState) {
    uiState.errorMessage?.let {
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = it, color = AccentCoral)
    }
    uiState.infoMessage?.let {
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = it, color = AccentBlue)
    }
}

private data class DashboardCopy(
    val heart: String,
    val blood: String,
    val temp: String,
    val chat: String,
    val alert: String,
    val causes: String
)

private fun copyFor(language: String): DashboardCopy {
    return when (language) {
        "zu" -> DashboardCopy(
            heart = "Ukushaya kwenhliziyo:",
            blood = "Umfutho wegazi:",
            temp = "Izinga lokushisa:",
            chat = "Xoxa ne-AI",
            alert = "KHOMBISA ISIXWAYISO ESIPHUTHUMAYO",
            causes = "Iziphakamiso Zempilo"
        )
        "af" -> DashboardCopy(
            heart = "Hartklop:",
            blood = "Bloeddruk:",
            temp = "Temperatuur:",
            chat = "Gesels met KI",
            alert = "WYS NOODWAARSKUWING",
            causes = "Welstandvoorstelle"
        )
        else -> DashboardCopy(
            heart = "Heart Rate:",
            blood = "Blood Pressure:",
            temp = "Temperature:",
            chat = "Chat with AI",
            alert = "SHOW EMERGENCY ALERT",
            causes = "Wellness Suggestions"
        )
    }
}
