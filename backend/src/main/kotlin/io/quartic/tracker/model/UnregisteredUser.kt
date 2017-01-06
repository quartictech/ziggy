package io.quartic.tracker.model

data class UnregisteredUser(override val id: UserId, val registrationCode: String) : User
