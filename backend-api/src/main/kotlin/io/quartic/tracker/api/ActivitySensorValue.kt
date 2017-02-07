package io.quartic.tracker.api

data class ActivitySensorValue(val activity: Int, val confidence: Int) : SensorValue
