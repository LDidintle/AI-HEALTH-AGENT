package za.ac.tut.healthmonitor.mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import za.ac.tut.healthmonitor.mobile.ui.theme.HealthMonitorTheme

class OnboardingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HealthMonitorTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "This app reads only the Health Connect vitals you approve, saves the latest synced values to your SmartHealth account, and shows non-diagnostic wellness suggestions. You can manage or remove Health Connect access from your phone settings at any time.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(
                        onClick = {
                            startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                            finish()
                        },
                        modifier = Modifier.padding(top = 20.dp)
                    ) {
                        Text("Open App")
                    }
                }
            }
        }
    }
}
