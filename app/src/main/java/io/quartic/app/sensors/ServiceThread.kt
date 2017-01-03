package io.quartic.app.sensors

import android.content.ContentValues
import android.content.Context
import android.util.Log
import java.util.*

class ServiceThread(val context: Context) : Thread() {
    companion object {
        const val TAG = "ServiceThread"
    }

    override fun run() {
        Log.i(TAG, "sensor service")
        val locationProvider = FusedLocationProvider(context)
        val database = Database(context).writableDatabase
        locationProvider.get()
                .forEach { update ->
                    val contentValues = ContentValues()
                    contentValues.put("name", "location")
                    contentValues.put("value", "${update.latitude},${update.longitude}")
                    contentValues.put("timestamp", update.timestamp)
                    database.insertOrThrow("sensors", null, contentValues)
                    Log.i(TAG, "wrote to DB")
                }
    }

}