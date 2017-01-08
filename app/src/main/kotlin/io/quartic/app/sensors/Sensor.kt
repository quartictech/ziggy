package io.quartic.app.sensors

import android.content.Intent

interface Sensor {
    fun processIntent(intent: Intent, database: Database)
}