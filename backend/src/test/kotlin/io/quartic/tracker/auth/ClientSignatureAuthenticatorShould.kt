package io.quartic.tracker.auth

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.quartic.EC_PUBLIC_KEY
import io.quartic.common.core.SignatureUtils
import io.quartic.tracker.UserDirectory
import io.quartic.tracker.model.RegisteredUser
import io.quartic.tracker.model.UnregisteredUser
import io.quartic.tracker.model.User
import io.quartic.tracker.model.UserId
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.util.*

class ClientSignatureAuthenticatorShould {
    private val directory = mock<UserDirectory>()
    private val authenticator = ClientSignatureAuthenticator(directory, true)

    private val input = "abcdefghihjklmnop"
    private val keyPair = SignatureUtils.generateRSAKeyPair()
    private val signature = SignatureUtils.sign(keyPair.private, input.toByteArray())
    private val user = RegisteredUser(mock<UserId>(), keyPair.public)

    @Test
    fun reject_if_unknown_user() {
        val userId = mock<UserId>()
        whenever(directory.getUser(any())).thenReturn(null)

        val creds = ClientSignatureCredentials(userId, "foo".toByteArray(), "bar".toByteArray())
        assertFalse(authenticator.authenticate(creds).isPresent)
    }

    @Test
    fun reject_if_unregistered_user() {
        val userId = mock<UserId>()
        whenever(directory.getUser(any())).thenReturn(UnregisteredUser(userId, "1234"))

        val creds = ClientSignatureCredentials(userId, "foo".toByteArray(), "bar".toByteArray())
        assertFalse(authenticator.authenticate(creds).isPresent)
    }

    @Test
    fun accept_if_signature_verified_by_public_key_for_registered_user() {
        whenever(directory.getUser(user.id)).thenReturn(user)

        val creds = ClientSignatureCredentials(user.id, signature, input.toByteArray())
        assertThat(authenticator.authenticate(creds), equalTo(Optional.of(user as User)))
    }

    @Test
    fun reject_if_signature_mismatch() {
        whenever(directory.getUser(user.id)).thenReturn(user)

        val creds = ClientSignatureCredentials(user.id, signature, (input + "X").toByteArray())
        assertFalse(authenticator.authenticate(creds).isPresent)
    }

    @Test
    fun reject_rather_than_throw_if_signature_undecodable() {
        whenever(directory.getUser(user.id)).thenReturn(user)

        val creds = ClientSignatureCredentials(user.id, "abcdefg".toByteArray(), input.toByteArray())
        assertFalse(authenticator.authenticate(creds).isPresent)
    }

    @Test
    fun accept_if_signature_mismatch_but_verification_disabled() {
        val authenticator = ClientSignatureAuthenticator(directory, false)

        whenever(directory.getUser(user.id)).thenReturn(user)

        val creds = ClientSignatureCredentials(user.id, signature, (input + "X").toByteArray())
        assertThat(authenticator.authenticate(creds), equalTo(Optional.of(user as User)))
    }
}