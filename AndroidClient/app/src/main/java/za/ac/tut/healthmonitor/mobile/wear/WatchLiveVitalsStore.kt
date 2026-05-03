package za.ac.tut.healthmonitor.mobile.wear

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import za.ac.tut.healthmonitor.mobile.data.model.HealthSyncPayload

object WatchLiveVitalsStore {
    private val _readings = MutableSharedFlow<HealthSyncPayload>(
        replay = 0,
        extraBufferCapacity = 8
    )
    val readings: SharedFlow<HealthSyncPayload> = _readings.asSharedFlow()

    fun publish(payload: HealthSyncPayload) {
        _readings.tryEmit(payload)
    }
}
