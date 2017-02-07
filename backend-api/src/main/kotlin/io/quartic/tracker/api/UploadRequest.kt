package io.quartic.tracker.api

data class UploadRequest(
        val timestamp: Long,
        val appVersionCode: Int,
        val appVersionName: String,
        val backlogSize: Int,
        val batteryLevel: Int,
        val values: List<SensorReading>,
        val deviceInformation: DeviceInformation
)