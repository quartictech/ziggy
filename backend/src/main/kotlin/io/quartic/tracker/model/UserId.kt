package io.quartic.tracker.model

import com.fasterxml.jackson.annotation.JsonValue

data class UserId(val uid: Long) {
    constructor(uid: String) : this(uid.toLong())

    @JsonValue
    override fun toString() = uid.toString()
}