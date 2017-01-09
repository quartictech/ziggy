package io.quartic.app.sync

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.quartic.app.state.ApplicationConfiguration
import io.quartic.app.R
import io.quartic.app.api.BackendApi
import io.quartic.app.authClientOf
import io.quartic.app.clientOf
import io.quartic.app.sensors.Database
import io.quartic.app.state.ApplicationState
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
        val backend = ApplicationState(context.applicationContext, config).authClient
        Database(context).processSensorData(1000, { sensorValues ->
            Log.i(TAG, "syncing ${sensorValues.size} values")
            try {
                backend.upload(UploadRequest(sensorValues)).toBlocking().first()
                Log.i(TAG, "uploaded ${sensorValues.size} values")
            }
            catch (e: Exception) {
                Log.e(TAG, "error uploading: ${e.message}")
                throw e
            }
        })
    }

}
