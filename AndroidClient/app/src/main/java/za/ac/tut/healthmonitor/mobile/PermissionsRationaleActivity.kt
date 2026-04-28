package za.ac.tut.healthmonitor.mobile

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

class PermissionsRationaleActivity : ComponentActivity() {

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
                        text = "Health permissions are used only to read your latest approved vitals from Health Connect and save them to your SmartHealth account for display, review, and non-diagnostic alerts. This app does not diagnose disease or replace professional medical care.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(
                        onClick = { finish() },
                        modifier = Modifier.padding(top = 20.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
