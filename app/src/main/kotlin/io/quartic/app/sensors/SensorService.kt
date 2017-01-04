package io.quartic.app.sensors

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import io.quartic.app.sync.AccountSingleton
import android.content.ContentResolver

class SensorService : Service() {
    var thread : ServiceThread? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "start command: ${intent}")
        if (intent != null) {
            thread!!.processIntent(intent)
        }
        return START_STICKY
    }

    companion object {
        const val TAG = "SensorService"
        fun startService(context: Context) {
            Log.i(TAG, "starting SensorService")
            context.startService(Intent(context.applicationContext, SensorService::class.java))
        }
    }

    override fun onCreate() {
        if (thread == null) {
            Log.d(TAG, "Launching thread")
            thread = ServiceThread(applicationContext)
            thread!!.start()
        }

        Log.i(TAG, "requesting sync")
        val settingsBundle = Bundle()
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true)
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true)

        ContentResolver.requestSync(
                AccountSingleton.getAccount(applicationContext),
                "io.quartic.app.provider",
                settingsBundle)

    }

    override fun onBind(intent: Intent?): IBinder {
        throw UnsupportedOperationException("not implemented")
    }
}


