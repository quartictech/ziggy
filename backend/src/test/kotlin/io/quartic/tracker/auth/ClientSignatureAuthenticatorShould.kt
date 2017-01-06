package io.quartic.tracker.auth

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.quartic.tracker.Store
import io.quartic.tracker.model.RegisteredUser
import io.quartic.tracker.model.UnregisteredUser
import io.quartic.tracker.model.User
import io.quartic.tracker.model.UserId
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.*

class ClientSignatureAuthenticatorShould {
    private val store = mock<Store>()
    private val authenticator = ClientSignatureAuthenticator(store)

    // These vectors were generated painfully from the Android app
    private val input = "abcdefghihjklmnop"
    private val signature = intArrayOf(0x30, 0x44, 0x02, 0x20, 0x35, 0x6b, 0xba, 0x4e, 0x6c, 0xbd, 0x66, 0x2a, 0x6f, 0xe3, 0x5c, 0x78, 0x8e, 0x51, 0x53, 0x3c, 0xa1, 0x84, 0xf5, 0x20, 0x7c, 0x3d, 0x49, 0x5d, 0xc0, 0x24, 0xdf, 0xf6, 0x2c, 0x47, 0x91, 0xc2, 0x02, 0x20, 0x05, 0x81, 0x79, 0xcc, 0x55, 0x8c, 0xbf, 0xe5, 0x7f, 0x68, 0xda, 0x07, 0x43, 0x89, 0x60, 0xe0, 0xd4, 0x9e, 0x75, 0xaa, 0x31, 0x00, 0xd8, 0xdd, 0x79, 0x5c, 0xf2, 0x22, 0xde, 0x41, 0x7d, 0x05)
            .map { it.toByte() }
            .toByteArray()
    private val publicKey = KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(
            intArrayOf(0x30, 0x59, 0x30, 0x13, 0x06, 0x07, 0x2a, 0x86, 0x48, 0xce, 0x3d, 0x02, 0x01, 0x06, 0x08, 0x2a, 0x86, 0x48, 0xce, 0x3d, 0x03, 0x01, 0x07, 0x03, 0x42, 0x00, 0x04, 0xfa, 0x2f, 0xa9, 0x16, 0x5b, 0xcf, 0x87, 0x39, 0x8d, 0xca, 0xa2, 0x49, 0x4d, 0xcc, 0x97, 0x89, 0x05, 0xbc, 0x9a, 0x93, 0x9e, 0xf6, 0x36, 0x8c, 0x98, 0x26, 0xf0, 0x16, 0x4f, 0x03, 0x55, 0x58, 0x4b, 0x3b, 0x9c, 0xd1, 0x37, 0xa0, 0xd9, 0x29, 0x17, 0xb3, 0x9c, 0xb8, 0xf7, 0x20, 0x65, 0x7c, 0x73, 0x6d, 0x9e, 0xf3, 0xb8, 0x21, 0x0e, 0x9e, 0xf6, 0x2c, 0x64, 0x2c, 0xa4, 0x1b, 0xc8, 0xb4)
                    .map { it.toByte() }
                    .toByteArray()
    ))

    private val user = RegisteredUser(mock<UserId>(), publicKey)

    @Test
    fun reject_if_unknown_user() {
        val userId = mock<UserId>()
        whenever(store.getUser(any())).thenReturn(null)

        val creds = ClientSignatureCredentials(userId, "foo".toByteArray(), "bar".toByteArray())
        assertFalse(authenticator.authenticate(creds).isPresent)
    }

    @Test
    fun reject_if_unregistered_user() {
        val userId = mock<UserId>()
        whenever(store.getUser(any())).thenReturn(UnregisteredUser(userId, "1234"))

        val creds = ClientSignatureCredentials(userId, "foo".toByteArray(), "bar".toByteArray())
        assertFalse(authenticator.authenticate(creds).isPresent)
    }

    @Test
    fun accept_if_signature_verified_by_public_key_for_registered_user() {
        whenever(store.getUser(user.id)).thenReturn(user)

        val creds = ClientSignatureCredentials(user.id, signature, input.toByteArray())
        assertThat(authenticator.authenticate(creds), equalTo(Optional.of(user as User)))
    }

    @Test
    fun reject_if_signature_mismatch() {
        whenever(store.getUser(user.id)).thenReturn(user)

        val creds = ClientSignatureCredentials(user.id, signature, (input + "X").toByteArray())
        assertFalse(authenticator.authenticate(creds).isPresent)
    }

    @Test
    fun reject_rather_than_throw_if_signature_undecodable() {
        whenever(store.getUser(user.id)).thenReturn(user)

        val creds = ClientSignatureCredentials(user.id, "abcdefg".toByteArray(), input.toByteArray())
        assertFalse(authenticator.authenticate(creds).isPresent)
    }
}