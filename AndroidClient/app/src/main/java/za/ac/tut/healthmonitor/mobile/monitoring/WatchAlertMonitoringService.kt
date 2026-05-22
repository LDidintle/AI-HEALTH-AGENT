package za.ac.tut.healthmonitor.mobile.monitoring

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import za.ac.tut.healthmonitor.mobile.MainActivity
import za.ac.tut.healthmonitor.mobile.R
import za.ac.tut.healthmonitor.mobile.data.model.HealthSectionSyncPayload
import za.ac.tut.healthmonitor.mobile.data.model.HealthSyncPayload
import za.ac.tut.healthmonitor.mobile.data.repository.AppRepository
import za.ac.tut.healthmonitor.mobile.health.SamsungHealthDataManager

class WatchAlertMonitoringService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var monitoringJob: Job? = null
    private var lastRunAtMillis = 0L

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification("Monitoring watch alerts", "Checks every 15 minutes to protect battery."))
        startMonitoringLoop()
        return START_STICKY
    }

    override fun onDestroy() {
        monitoringJob?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startMonitoringLoop() {
        if (monitoringJob?.isActive == true) {
            return
        }

        monitoringJob = serviceScope.launch {
            val repository = AppRepository(applicationContext)
            val samsungHealth = SamsungHealthDataManager(applicationContext)

            while (isActive) {
                val now = System.currentTimeMillis()
                if (BackgroundMonitorPolicy.shouldRun(lastRunAtMillis, now)) {
                    lastRunAtMillis = now
                    runMonitoringCycle(repository, samsungHealth)
                }
                delay(60_000L)
            }
        }
    }

    private suspend fun runMonitoringCycle(
        repository: AppRepository,
        samsungHealth: SamsungHealthDataManager
    ) {
        try {
            if (!samsungHealth.hasAnyRequiredPermission()) {
                updateNotification("Watch monitoring paused", "Open SmartHealth once to allow Samsung Health permissions.")
                return
            }

            val section = samsungHealth.readLatestSection()
            if (!section.isEmpty()) {
                persistSection(repository, section.payload)
            }

            val alert = repository.getAlertNotification()
            if (alert.hasAlert && alert.alert != null) {
                updateNotification("Emergency alert detected", alert.alert.message ?: "Open SmartHealth to view the alert.")
            } else {
                updateNotification("Monitoring watch alerts", "Last check completed. Next check runs in about 15 minutes.")
            }
        } catch (_: Exception) {
            updateNotification("Monitoring watch alerts", "Background check will retry automatically.")
        }
    }

    private fun persistSection(repository: AppRepository, payload: HealthSectionSyncPayload) {
        try {
            repository.syncHealthSection(payload)
        } catch (_: Exception) {
            repository.syncReadings(payload.toHealthSyncPayload())
        }
    }

    private fun HealthSectionSyncPayload.toHealthSyncPayload(): HealthSyncPayload {
        return HealthSyncPayload(
            heartRate = heartRateLatest,
            temperature = temperatureLatest,
            systolic = systolicLatest,
            diastolic = diastolicLatest,
            source = source,
            recordedAt = windowEnd,
            externalRecordId = "section-$windowEnd",
            deviceType = deviceType,
            deviceManufacturer = deviceManufacturer,
            deviceModel = deviceModel
        )
    }

    private fun updateNotification(title: String, text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(title, text))
    }

    private fun buildNotification(title: String, text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_smarthealth)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Watch alert monitoring",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Persistent SmartHealth watch alert monitoring."
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "watch_alert_monitoring"
        private const val NOTIFICATION_ID = 2405

        fun start(context: Context) {
            val intent = Intent(context, WatchAlertMonitoringService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, WatchAlertMonitoringService::class.java))
        }
    }
}
