package io.quartic.app.sensors

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import io.quartic.app.model.LocationUpdate
import rx.Observable
import rx.subjects.PublishSubject
import java.util.*

class FusedLocationProvider(private val context: Context) :
        LocationProvider, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    companion object {
        const val TAG = "FusedLocationProvider"
    }

    private val apiClient: GoogleApiClient
    private val subject: PublishSubject<io.quartic.app.model.LocationUpdate>

    init {
        Log.i(TAG, "connecting to google play APIs")
        Log.i(TAG, this.toString())
        this.apiClient = GoogleApiClient.Builder(context.applicationContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        apiClient.connect()
        Log.d(TAG, "done")
        this.subject = PublishSubject.create()
    }

    override fun onConnected(bundle: Bundle?) {
        Log.i(TAG, "requesting location updates")
        val locationRequest = LocationRequest.create()
                .setInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(1000)
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, this)
        Log.i(TAG, "waiting")
    }

    override fun onConnectionSuspended(i: Int) {
        Log.i(TAG, "connection suspended")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.e(TAG, "connection to play services failed: " + connectionResult.errorMessage)
    }

    override fun onLocationChanged(location: Location) {
        Log.i(TAG, "location update received" + location)
        subject.onNext(LocationUpdate(location.latitude, location.longitude, Date().time))
    }

    override fun get(): Observable<io.quartic.app.model.LocationUpdate> {
        return subject
    }
}
