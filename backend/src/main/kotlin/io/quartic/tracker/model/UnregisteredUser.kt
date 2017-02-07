package io.quartic.tracker.model

import io.quartic.tracker.api.UserId

data class UnregisteredUser(override val id: UserId, val registrationCode: String) : User
