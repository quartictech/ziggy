package io.quartic.tracker.model

data class UnregisteredUser(val id: UserId, val registrationCode: String) : User

