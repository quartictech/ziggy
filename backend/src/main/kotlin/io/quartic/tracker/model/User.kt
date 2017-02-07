package io.quartic.tracker.model

import io.quartic.tracker.api.UserId
import java.security.Principal

interface User : Principal {
    val id: UserId

    override fun getName(): String {
        return id.uid.toString()
    }
}
