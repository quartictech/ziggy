package io.quartic.app.sensors

import android.content.Intent
import io.quartic.app.storage.Database

interface Sensor {
    fun processIntent(intent: Intent, database: Database)


}