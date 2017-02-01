package io.quartic.app.sync

import android.accounts.Account
import android.app.Service
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Intent
import android.content.SyncResult
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import io.quartic.app.ApplicationConfiguration
import io.quartic.app.state.ApplicationState
import io.quartic.app.tag

class SyncService : Service() {
    val TAG by tag()
    private val syncAdapterLock: Any = Any()
    private lateinit var syncAdapter: AbstractThreadedSyncAdapter

    override fun onCreate() {
        Log.i(TAG, "creating SyncService")
        synchronized(syncAdapterLock) {
            syncAdapter = MySyncAdapter()
        }
    }

    private inner class MySyncAdapter : AbstractThreadedSyncAdapter(applicationContext, true) {
        private val config = ApplicationConfiguration.load(context.applicationContext)
        private val state = ApplicationState(context.applicationContext, config)
        private val uploader = BatchUploader(state,
                { getBatteryLevel(context) },
                { System.currentTimeMillis() },
                { getDeviceInformation() })

        override fun onPerformSync(account: Account?, extras: Bundle?, authority: String?, provider: ContentProviderClient?, syncResult: SyncResult?) {
            uploader.upload()
        }
    }

    override fun onBind(intent: Intent?): IBinder = syncAdapter.syncAdapterBinder
}