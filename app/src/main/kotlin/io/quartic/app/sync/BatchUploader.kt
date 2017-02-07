package io.quartic.app.sync

import android.util.Log
import io.quartic.app.BuildConfig
import io.quartic.app.state.ApplicationState
import io.quartic.app.tag
import io.quartic.tracker.api.DeviceInformation
import io.quartic.tracker.api.SensorReading
import io.quartic.tracker.api.UploadRequest
import retrofit2.adapter.rxjava.HttpException

class BatchUploader(
        private val state: ApplicationState,
        private val getBatteryLevel: () -> Int,
        private val getCurrentTimeMillis: () -> Long,    // TODO: how do we inject a proper clock?
        private val getDeviceInformation: () -> DeviceInformation
) {
    private val TAG by tag()

    fun upload() {
        if (state.userId != null) {
            Log.i(TAG, "Starting sync")
            state.lastAttemptedSyncTime = getCurrentTimeMillis()

            while (state.database.backlogSize > 0) {
                if (!syncBatch()) {
                    return
                }
            }
            state.lastSyncTime = getCurrentTimeMillis()
            state.numConsecutiveSyncAuthFailures = 0
        }
        else {
            Log.i(TAG, "User needs to authenticate")
        }
    }

    private fun syncBatch(): Boolean {
        val backend = state.authClient
        val sensorValues = state.database.sensorValues
        Log.i(TAG, "Syncing ${sensorValues.size} values")

        var success = false
        backend.upload(UploadRequest(
                timestamp = getCurrentTimeMillis(),
                appVersionCode = BuildConfig.VERSION_CODE,
                appVersionName = BuildConfig.VERSION_NAME,
                batteryLevel = getBatteryLevel(),
                backlogSize = state.database.backlogSize,
                values = sensorValues,
                deviceInformation = getDeviceInformation()
        )).subscribe(
                { handleSuccess(sensorValues); success = true },
                { handleFailure(it); success = false }
        )
        return success
    }

    private fun handleSuccess(values: List<SensorReading>) {
        Log.i(TAG, "Uploaded ${values.size} values")
        state.database.delete(values.map(SensorReading::id))
    }

    private fun handleFailure(t: Throwable) {
        Log.e(TAG, "Error uploading - will try again later", t)
        if (t is HttpException && t.code() == 401) {
            state.numConsecutiveSyncAuthFailures++
            Log.i(TAG, "Num consecutive auth failures: ${state.numConsecutiveSyncAuthFailures}")
            if (state.numConsecutiveSyncAuthFailures == MAX_CONSECUTIVE_AUTH_FAILURES) {
                Log.e(TAG, "Too many consecutive auth failures - dropping userId")
                state.userId = null
            }
        }
    }

    companion object {
        val MAX_CONSECUTIVE_AUTH_FAILURES = 3
    }
}