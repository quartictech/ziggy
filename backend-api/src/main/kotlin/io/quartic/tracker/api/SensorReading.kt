package io.quartic.tracker.api

data class SensorReading(val id: Int, val name: String, val value: SensorValue, val timestamp: Long)