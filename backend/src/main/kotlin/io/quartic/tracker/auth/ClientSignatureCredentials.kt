package io.quartic.tracker.auth

import io.quartic.tracker.api.UserId
import java.util.*

data class ClientSignatureCredentials(val userId: UserId, val signature: ByteArray, val request: ByteArray) {
    // We have to override because ByteArray is really a byte[]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as ClientSignatureCredentials

        return (userId == other.userId) &&
                Arrays.equals(signature, other.signature) &&
                Arrays.equals(request, other.request)
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + Arrays.hashCode(signature)
        result = 31 * result + Arrays.hashCode(request)
        return result
    }
}