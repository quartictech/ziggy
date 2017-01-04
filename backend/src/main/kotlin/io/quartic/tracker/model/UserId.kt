package io.quartic.tracker.model

import io.quartic.common.uid.Uid

class UserId(uid: String) : Uid(uid) {
    companion object {
        fun fromString(uid: String) = UserId(uid)
    }
}