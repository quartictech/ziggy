package io.quartic.tracker.resource

import io.quartic.tracker.model.UserId

data class MyCredentials(val userId: UserId, val signature: String, val request: String)