package io.quartic.tracker.auth

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.quartic.tracker.Store
import io.quartic.tracker.model.UnregisteredUser
import io.quartic.tracker.model.UserId
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.util.*

class ClientSignatureAuthenticatorShould {
    private val store = mock<Store>()
    private val authenticator = ClientSignatureAuthenticator(store)

    @Test
    internal fun reject_if_unknown_user() {
        whenever(store.getUser(any())).thenReturn(null)

        val creds = ClientSignatureCredentials(UserId("abc"), "foo", "bar".toByteArray())
        assertThat(authenticator.authenticate(creds), equalTo(Optional.empty<MyPrincipal>()))
    }

    @Test
    internal fun reject_if_unregistered_user() {
        whenever(store.getUser(any())).thenReturn(UnregisteredUser(UserId("abc"), "1234"))

        val creds = ClientSignatureCredentials(UserId("abc"), "foo", "bar".toByteArray())
        assertThat(authenticator.authenticate(creds), equalTo(Optional.empty<MyPrincipal>()))
    }
}