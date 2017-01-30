package io.quartic.app.sensors

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import io.quartic.app.ApplicationConfiguration
import io.quartic.app.state.ApplicationState
import io.quartic.app.storage.SensorContentProvider
import io.quartic.app.tag

class SensorService : Service() {
    companion object {
        val TAG by tag()
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

        val applicationState = ApplicationState(applicationContext,
                ApplicationConfiguration.load(applicationContext))

        if (applicationState.configuration.enableLiveUpload) {
            registerLiveUploadAlarm(applicationState)
        }

        if (applicationState.configuration.enablePeriodicUpload) {
            registerPeriodicUpload(applicationState)
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        throw UnsupportedOperationException("not implemented")
    }

    private fun registerPeriodicUpload(applicationState: ApplicationState) {
        // According to the docs this will update the current periodic sync if it is called repeatedly
        // See: https://developer.android.com/reference/android/content/ContentResolver.html
        ContentResolver.addPeriodicSync(
                applicationState.account,
                SensorContentProvider.PROVIDER_NAME,
                Bundle.EMPTY,
                applicationState.configuration.periodicUploadIntervalSeconds)
    }

    private fun registerLiveUploadAlarm(applicationState: ApplicationState) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                applicationState.configuration.liveUploadIntervalMilliseconds,
                pendingIntent)
    }
}


