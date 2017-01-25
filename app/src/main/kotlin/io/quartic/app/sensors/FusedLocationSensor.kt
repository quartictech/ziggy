package io.quartic.app.sensors

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import io.quartic.app.storage.Database
import io.quartic.app.tag

class FusedLocationSensor(context: Context) : GoogleApiClientSensor(context, LocationServices.API) {
    override val TAG by tag()
    private val apiClient = makeApiClient()

    override fun onConnected(p0: Bundle?) {
        val intent = Intent(context.applicationContext, SensorService::class.java)
        val pendingIntent = PendingIntent.getService(context.applicationContext, 0, intent, 0)
        val locationRequest = LocationRequest.create()
                .setInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(1000)
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, pendingIntent)
    }

    override fun processIntent(intent: Intent, database: Database) {
        if (LocationResult.hasResult(intent)) {
           processLocationUpdate(LocationResult.extractResult(intent)!!, database)
        }
    }

    private fun processLocationUpdate(result: LocationResult, database: Database) {
        Log.i(TAG, "writing to db: $result")
        database.writeSensor(
                "location",
                "${result.lastLocation.latitude}, ${result.lastLocation.longitude}",
                result.lastLocation.time
        )
    }
}