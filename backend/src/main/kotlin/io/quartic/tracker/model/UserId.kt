package io.quartic.tracker.model

import com.fasterxml.jackson.annotation.JsonValue

data class UserId(@get:JsonValue val uid: Long) {
    constructor(uid: String) : this(uid.toLong())
}