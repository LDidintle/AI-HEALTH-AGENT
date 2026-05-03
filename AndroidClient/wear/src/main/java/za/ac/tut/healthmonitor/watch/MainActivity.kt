package za.ac.tut.healthmonitor.watch

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.wearable.Wearable
import org.json.JSONObject
import java.time.Instant

class MainActivity : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private lateinit var statusText: TextView
    private lateinit var valueText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        setContentView(buildContentView())

        if (hasBodySensorPermission()) {
            startHeartRate()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BODY_SENSORS),
                BODY_SENSOR_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == BODY_SENSOR_REQUEST_CODE &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            startHeartRate()
        } else {
            statusText.text = "Body sensor permission is required."
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasBodySensorPermission()) {
            startHeartRate()
        }
    }

    override fun onPause() {
        sensorManager.unregisterListener(this)
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_HEART_RATE) {
            return
        }

        val heartRate = event.values.firstOrNull()?.toInt()?.takeIf { it > 0 } ?: return
        valueText.text = "$heartRate BPM"
        statusText.text = "Sending to phone"
        sendHeartRateToPhone(heartRate)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun startHeartRate() {
        val sensor = heartRateSensor
        if (sensor == null) {
            statusText.text = "Heart-rate sensor not found."
            return
        }

        statusText.text = "Reading watch sensor..."
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun sendHeartRateToPhone(heartRate: Int) {
        val payload = JSONObject()
            .put("heartRate", heartRate)
            .put("recordedAt", Instant.now().toString())
            .put("externalRecordId", "watch-heart-${System.currentTimeMillis()}")
            .put("deviceModel", android.os.Build.MODEL)
            .toString()
            .toByteArray(Charsets.UTF_8)

        Wearable.getNodeClient(this).connectedNodes
            .addOnSuccessListener { nodes ->
                nodes.forEach { node ->
                    Wearable.getMessageClient(this)
                        .sendMessage(node.id, LIVE_VITALS_PATH, payload)
                }
                statusText.text = if (nodes.isEmpty()) {
                    "Open the phone app nearby."
                } else {
                    "Sent to phone"
                }
            }
            .addOnFailureListener {
                statusText.text = "Phone connection failed."
            }
    }

    private fun hasBodySensorPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun buildContentView(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(18, 18, 18, 18)
            setBackgroundColor(Color.rgb(2, 13, 11))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            valueText = TextView(context).apply {
                text = "-- BPM"
                textSize = 28f
                setTextColor(Color.rgb(255, 221, 89))
                gravity = Gravity.CENTER
            }
            statusText = TextView(context).apply {
                text = "Starting..."
                textSize = 14f
                setTextColor(Color.rgb(207, 235, 221))
                gravity = Gravity.CENTER
            }

            addView(valueText)
            addView(statusText)
        }
    }

    private companion object {
        const val BODY_SENSOR_REQUEST_CODE = 42
        const val LIVE_VITALS_PATH = "/smarthealth/live-vitals"
    }
}
