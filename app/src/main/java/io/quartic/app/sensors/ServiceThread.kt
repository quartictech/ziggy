package io.quartic.app.sensors

import android.content.Context
import android.util.Log

class ServiceThread(val context: Context) : Thread() {
    companion object {
        const val TAG = "ServiceThread"
    }

    override fun run() {
        Log.i(TAG, "sensor service")
        val locationProvider = FusedLocationProvider(context)
        locationProvider.get()
                .forEach { update ->
                    Database(context).writeSensor("location",
                            "${update.latitude},${update.longitude}",
                            update.timestamp)
                    Log.i(TAG, "wrote to DB")
                }
    }

}