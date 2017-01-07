package io.quartic.app.sensors

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionResult

class ActivitySensor(val context: Context) : Sensor, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    override fun processIntent(intent: Intent, database: Database) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            processUpdate(ActivityRecognitionResult.extractResult(intent), database)
        }
    }

    private fun processUpdate(result: ActivityRecognitionResult, database: Database) {
        Log.i(TAG, "writing acitivity update")
        database.writeSensor("activity",
                result.mostProbableActivity.type.toString(),
                result.time)
    }

    companion object {
        const val TAG = "ActivitySensor"
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnected(p0: Bundle?) {
        val intent = Intent(context.applicationContext, SensorService::class.java)
        val pendingIntent = PendingIntent.getService(context.applicationContext, 0, intent, 0)
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(apiClient, 5000, pendingIntent)
    }

    override fun onConnectionSuspended(p0: Int) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val apiClient: GoogleApiClient

    init {
        Log.i(TAG, "connecting to google play APIs")
        this.apiClient = GoogleApiClient.Builder(context.applicationContext)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        apiClient.connect()
    }
}
