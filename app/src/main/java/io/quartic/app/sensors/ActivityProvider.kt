package io.quartic.app.sensors

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.ActivityRecognition
import rx.subjects.PublishSubject

class ActivityProvider(val context: Context) : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    companion object {
        const val TAG = "ActivityProvider"
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnected(p0: Bundle?) {
        val intent = Intent(this,
                PendingIntent.getService()
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(apiClient, 5000, )
    }

    override fun onConnectionSuspended(p0: Int) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val apiClient: GoogleApiClient
    private val subject: PublishSubject<ActivityUpdate>

    init {
        Log.i(TAG, "connecting to google play APIs")
        this.apiClient = GoogleApiClient.Builder(context.applicationContext)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        apiClient.connect()
        this.subject = PublishSubject.create()
    }
}
