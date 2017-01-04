package io.quartic.tracker.auth

import io.quartic.tracker.model.UserId
import java.util.*

data class ClientCertCredentials(val userId: UserId, val signature: String, val request: ByteArray) {
    // We have to override because ByteArray is really a byte[]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as ClientCertCredentials

        if (userId != other.userId) return false
        if (signature != other.signature) return false
        return Arrays.equals(request, other.request)
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + signature.hashCode()
        result = 31 * result + Arrays.hashCode(request)
        return result
    }
}