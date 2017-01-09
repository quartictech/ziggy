package io.quartic.tracker.model

import com.fasterxml.jackson.annotation.JsonValue

data class UserId(val uid: Long) {
    @JsonValue
    override fun toString() = uid.toString()

    companion object {
        fun fromString(uid: String) = UserId(uid.toLong())
    }
}