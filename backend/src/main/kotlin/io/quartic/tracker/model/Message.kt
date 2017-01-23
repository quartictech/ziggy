package io.quartic.tracker.model

import io.quartic.tracker.api.SensorValue
import java.time.Instant

data class Message(
        val userId: UserId,
        val timestamp: Instant,
        val readings: List<SensorValue>
)