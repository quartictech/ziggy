package io.quartic.app.sensors

import android.content.Context
import android.content.Intent
import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.GoogleApiClient

interface Sensor {
    fun processIntent(intent: Intent, database: Database)


}