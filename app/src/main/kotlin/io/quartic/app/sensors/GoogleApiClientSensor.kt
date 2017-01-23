package io.quartic.app.sensors

import android.content.Context
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.GoogleApiClient

abstract class GoogleApiClientSensor(val context: Context, val api: Api<out Api.ApiOptions.NotRequiredOptions>) : Sensor,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    companion object {
        val TAG = GoogleApiClientSensor::class.java.name
    }

    fun makeApiClient(): GoogleApiClient {
        Log.i(TAG, "connecting to google play APIs")
        val apiClient = GoogleApiClient.Builder(context.applicationContext)
                .addApi(api)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        apiClient.connect()
        return apiClient
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.e(TAG, "connection suspended")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.e(TAG, "connection failed")
    }

}
