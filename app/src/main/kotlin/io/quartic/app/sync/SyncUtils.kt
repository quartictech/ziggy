package io.quartic.app.sync

import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import io.quartic.app.ApplicationConfiguration
import io.quartic.app.state.ApplicationState
import io.quartic.app.storage.SensorContentProvider
import android.os.BatteryManager
import android.content.Intent
import android.content.IntentFilter

fun forceSync(context: Context) {
    val settingsBundle = Bundle()
        with(settingsBundle) {
            putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
            putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
        }

    val applicationState = ApplicationState(context.applicationContext,
            ApplicationConfiguration.load(context.applicationContext))

    if (applicationState.account != null) {
        ContentResolver.requestSync(
                applicationState.account,
                SensorContentProvider.PROVIDER_NAME,
                settingsBundle)
    }
}

fun getBatteryLevel(context: Context): Int {
    val batIntentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val battery = context.registerReceiver(null, batIntentFilter)
    return battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
}