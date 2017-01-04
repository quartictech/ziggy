package io.quartic.app.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class SyncService : Service() {
    companion object {
        const val TAG = "SyncService"
    }
    private val syncAdapterLock: Any = Any()
    private var syncAdapter: SyncAdapter? = null

    override fun onCreate() {
        Log.i(TAG, "creating SyncService")
        synchronized(syncAdapterLock) {
            syncAdapter = SyncAdapter(applicationContext, true)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return syncAdapter!!.syncAdapterBinder
    }
}