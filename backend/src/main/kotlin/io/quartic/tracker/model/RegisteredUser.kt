package io.quartic.tracker.model

import java.security.PublicKey

data class RegisteredUser(val id: UserId, val publicKey: PublicKey) : User
