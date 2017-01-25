package io.quartic.tracker.api

import java.time.Instant

data class UploadRequest(
        val timestamp: Long,
        val backlogSize: Int,
        val batteryLevel: Int,
        val values: List<SensorValue>)