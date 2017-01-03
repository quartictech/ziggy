package io.quartic.app.sensors

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log

class SensorService : Service() {
    var thread : ServiceThread? = null

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
    }

    override fun onBind(intent: Intent?): IBinder {
        throw UnsupportedOperationException("not implemented")
    }
}


