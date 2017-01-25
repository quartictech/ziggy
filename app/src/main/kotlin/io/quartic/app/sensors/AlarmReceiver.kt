package io.quartic.app.sensors

import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import io.quartic.app.ApplicationConfiguration
import io.quartic.app.state.ApplicationState
import io.quartic.app.tag

class AlarmReceiver : BroadcastReceiver() {
    val TAG by tag()

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, "alarm fired")
        val settingsBundle = Bundle()
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true)
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true)

        val applicationState = ApplicationState(context!!.applicationContext,
                ApplicationConfiguration.load(context.applicationContext))
        ContentResolver.requestSync(
                applicationState.account,
                "io.quartic.app.provider",
                settingsBundle)
    }
}