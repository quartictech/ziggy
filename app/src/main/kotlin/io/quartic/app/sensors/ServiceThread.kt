package io.quartic.app.sensors

import android.content.Context
import android.content.Intent
import android.util.Log
import io.quartic.app.ApplicationConfiguration
import io.quartic.app.state.ApplicationState
import io.quartic.app.tag

class ServiceThread(val context: Context) : Thread() {
    val TAG by tag()

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