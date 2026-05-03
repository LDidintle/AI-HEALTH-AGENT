package za.ac.tut.healthmonitor.mobile.wear

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import org.json.JSONObject
import za.ac.tut.healthmonitor.mobile.data.model.HealthSyncPayload

class WatchLiveVitalsListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path != LIVE_VITALS_PATH) {
            return
        }

        val json = JSONObject(String(messageEvent.data, Charsets.UTF_8))
        WatchLiveVitalsStore.publish(
            HealthSyncPayload(
                heartRate = json.optNullableInt("heartRate"),
                temperature = json.optNullableDouble("temperature"),
                systolic = json.optNullableInt("systolic"),
                diastolic = json.optNullableInt("diastolic"),
                source = "GALAXY_WATCH_LIVE",
                recordedAt = json.optString("recordedAt").takeIf { it.isNotBlank() },
                externalRecordId = json.optString("externalRecordId").takeIf { it.isNotBlank() },
                deviceType = "WATCH",
                deviceManufacturer = "Samsung",
                deviceModel = json.optString("deviceModel").takeIf { it.isNotBlank() }
            )
        )
    }

    private fun JSONObject.optNullableInt(name: String): Int? {
        return if (has(name) && !isNull(name)) optInt(name) else null
    }

    private fun JSONObject.optNullableDouble(name: String): Double? {
        return if (has(name) && !isNull(name)) optDouble(name) else null
    }

    private companion object {
        const val LIVE_VITALS_PATH = "/smarthealth/live-vitals"
    }
}
