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
        val sensorValues = Database(context).getUnsavedSensorData(100)
        Log.i(TAG, "syncing ${sensorValues.size} values")
        val config = ApplicationConfiguration.load(context.applicationContext)
        val backend = ApplicationState(context.applicationContext, config).authClient

        backend.upload(UploadRequest(sensorValues)).subscribe(
                { v -> Log.i(TAG, "uploaded ${sensorValues.size} values") },
                { e -> Log.e(TAG, "error uploading: ${e.message}")}
        )
    }

}
