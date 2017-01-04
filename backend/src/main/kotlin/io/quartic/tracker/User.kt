package io.quartic.tracker

sealed class User(val id: UserId) {
    class UnregisteredUser(userId: UserId, val registrationCode: String) : User(userId)
    class RegisteredUser(userId: UserId, val base64EncodedPublicKey: String) : User(userId)
}