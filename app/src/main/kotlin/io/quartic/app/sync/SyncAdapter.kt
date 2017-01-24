package io.quartic.app.sync

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import android.util.Log
import io.quartic.app.ApplicationConfiguration
import io.quartic.app.state.ApplicationState
import io.quartic.tracker.api.SensorValue
import io.quartic.tracker.api.UploadRequest

class SyncAdapter(context: Context?, autoInitialize: Boolean) :
        AbstractThreadedSyncAdapter(context, autoInitialize) {
    companion object {
        const val TAG = "SyncAdapter"
    }

    override fun onPerformSync(account: Account?, extras: Bundle?, authority: String?,
                               provider: ContentProviderClient?, syncResult: SyncResult?) {
        Log.i(TAG, "starting sync")
        val config = ApplicationConfiguration.load(context.applicationContext)
        val applicationState = ApplicationState(context.applicationContext, config)
        val backend = applicationState.authClient

        val sensorValues = applicationState.database.getSensorValues()
        Log.i(TAG, "syncing ${sensorValues.size} values")
        try {
            backend.upload(UploadRequest(sensorValues)).toBlocking().first()
            applicationState.database.delete(sensorValues.map(SensorValue::id))
            Log.i(TAG, "uploaded ${sensorValues.size} values")
        }
        catch (e: Exception) {
            Log.e(TAG, "error uploading: ${e.message}. will try again later.")
        }
    }
}
