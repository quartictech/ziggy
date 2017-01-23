package io.quartic.app.sensors

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import io.quartic.app.state.ApplicationState

class SensorService : Service() {
    companion object {
        const val TAG = "SensorService"
        fun startService(context: Context) {
            Log.i(TAG, "starting SensorService")
            context.startService(Intent(context.applicationContext, SensorService::class.java))
        }
    }
    var thread : ServiceThread? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "start command: ${intent}")
        if (intent != null) {
            thread!!.processIntent(intent)
        }
        return START_STICKY
    }

    override fun onCreate() {
        if (thread == null) {
            Log.d(TAG, "Launching thread")
            thread = ServiceThread(applicationContext)
            thread!!.start()
        }

        // Schedule repeating sync
        // Note we should use SyncAdapter here but it won't allow faster than hourly updates
        // TODO: Use periodic SyncAdapter
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                ApplicationState.get(applicationContext).configuration.uploadIntervalMilliseconds,
                pendingIntent)
    }

    override fun onBind(intent: Intent?): IBinder {
        throw UnsupportedOperationException("not implemented")
    }
}


