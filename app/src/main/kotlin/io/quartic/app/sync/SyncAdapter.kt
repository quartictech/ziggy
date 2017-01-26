package io.quartic.app.sync

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import io.quartic.app.ApplicationConfiguration
import io.quartic.app.state.ApplicationState
import io.quartic.tracker.api.SensorValue
import io.quartic.tracker.api.UploadRequest
import android.content.Context.BATTERY_SERVICE
import io.quartic.app.tag

class SyncAdapter(context: Context?, autoInitialize: Boolean) :
        AbstractThreadedSyncAdapter(context, autoInitialize) {
    val TAG by tag()
    val config = ApplicationConfiguration.load(context!!.applicationContext)
    val applicationState = ApplicationState(context!!.applicationContext, config)

    override fun onPerformSync(account: Account?, extras: Bundle?, authority: String?,
                               provider: ContentProviderClient?, syncResult: SyncResult?) {
        if (applicationState.userId != null) {
            Log.i(TAG, "starting sync")
            while (applicationState.database.getBacklogSize() > 0) {
                syncBatch()
            }
        }
        else {
            Log.i(TAG, "user needs to authenticate")
        }
    }

    private fun syncBatch() {
        val backend = applicationState.authClient
        val sensorValues = applicationState.database.getSensorValues()
        Log.i(TAG, "syncing ${sensorValues.size} values")
        try {
            backend.upload(UploadRequest(
                    timestamp = System.currentTimeMillis(),
                    batteryLevel = getBatteryLevel(),
                    backlogSize = applicationState.database.getBacklogSize(),
                    values = sensorValues
            )).toBlocking().first()
            applicationState.database.delete(sensorValues.map(SensorValue::id))
            Log.i(TAG, "uploaded ${sensorValues.size} values")
        }
        catch (e: Exception) {
            Log.e(TAG, "error uploading: ${e.message}. will try again later.")
        }
    }

    private fun getBatteryLevel(): Int {
        val bm = context.applicationContext.getSystemService(BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
}
