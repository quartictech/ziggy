package io.quartic.tracker.model

import io.quartic.tracker.api.UserId
import java.security.PublicKey

data class RegisteredUser(override val id: UserId, val publicKey: PublicKey) : User
