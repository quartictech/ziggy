package io.quartic.tracker.model

import io.quartic.tracker.api.UploadRequest
import java.time.Instant

data class Message(
        val userId: UserId,
        val timestamp: Instant,
        val data: UploadRequest
)