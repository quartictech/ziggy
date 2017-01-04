package io.quartic.tracker.model

data class RegisteredUser(val id: UserId, val base64EncodedPublicKey: String) : User
