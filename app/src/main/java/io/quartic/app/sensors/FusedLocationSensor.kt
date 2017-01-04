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

class FusedLocationSensor(val context: Context) : Sensor, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    override fun onConnectionFailed(p0: ConnectionResult) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnected(p0: Bundle?) {
        val intent = Intent(context.applicationContext, SensorService::class.java)
        val pendingIntent = PendingIntent.getService(context.applicationContext, 0, intent, 0)
        val locationRequest = LocationRequest.create()
                .setInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(1000)
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, pendingIntent)
    }

    override fun onConnectionSuspended(p0: Int) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        const val TAG = "FusedLocationSensor"
    }

    private val apiClient: GoogleApiClient

    init {
        Log.i(TAG, "connecting to google play APIs")
        this.apiClient = GoogleApiClient.Builder(context.applicationContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        apiClient.connect()
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