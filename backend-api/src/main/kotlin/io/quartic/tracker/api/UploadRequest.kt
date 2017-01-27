package io.quartic.tracker.api

data class UploadRequest(
        val timestamp: Long,
        val backlogSize: Int,
        val batteryLevel: Int,
        val values: List<SensorValue>)