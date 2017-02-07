package io.quartic.tracker.api

import com.fasterxml.jackson.annotation.JsonValue

data class UserId(val uid: Long) {
    constructor(uid: String) : this(uid.toLong())

    @JsonValue
    override fun toString(): String {
        return uid.toString()
    }
}