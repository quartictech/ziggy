package io.quartic.app.sensors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.quartic.app.state.ApplicationState
import io.quartic.app.sync.forceSync
import io.quartic.app.tag

class AlarmReceiver : BroadcastReceiver() {
    val TAG by tag()

    override fun onReceive(context: Context, intent: Intent?) {
        Log.i(TAG, "alarm fired")
        forceSync(ApplicationState.get(context.applicationContext))
    }
}