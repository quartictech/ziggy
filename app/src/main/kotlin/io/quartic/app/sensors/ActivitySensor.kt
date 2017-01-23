package io.quartic.app.sensors

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionResult
import io.quartic.app.storage.Database

class ActivitySensor(context: Context) : GoogleApiClientSensor(context, ActivityRecognition.API) {
    companion object {
        const val TAG = "ActivitySensor"
    }
    private val apiClient = makeApiClient()

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

    override fun onConnected(p0: Bundle?) {
        val intent = Intent(context.applicationContext, SensorService::class.java)
        val pendingIntent = PendingIntent.getService(context.applicationContext, 0, intent, 0)
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(apiClient, 30000, pendingIntent)
    }
}
