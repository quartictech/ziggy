package io.quartic.app.sensors

import android.content.Context
import android.content.Intent
import android.util.Log
import io.quartic.app.ApplicationConfiguration
import io.quartic.app.state.ApplicationState

class ServiceThread(val context: Context) : Thread() {
    companion object {
        const val TAG = "ServiceThread"
    }

    private var sensors: List<Sensor>? = null
    private val applicationState = ApplicationState(context.applicationContext,
            ApplicationConfiguration.load(context.applicationContext))

    override fun run() {
        Log.i(TAG, "sensor service")
        sensors = arrayListOf(
                FusedLocationSensor(context),
                ActivitySensor(context)
        )
    }

    fun  processIntent(intent: Intent) {
        sensors?.map { sensor -> sensor.processIntent(intent, applicationState.database) }
    }
}