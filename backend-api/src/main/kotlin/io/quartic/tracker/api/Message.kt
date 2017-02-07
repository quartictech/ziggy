package io.quartic.tracker.api

import java.time.Instant

data class Message(
        val userId: io.quartic.tracker.api.UserId,
        val timestamp: Instant,
        val data: UploadRequest
)