package io.quartic.tracker.resource

import com.nhaarman.mockito_kotlin.*
import io.quartic.EC_PUBLIC_KEY
import io.quartic.RSA_PUBLIC_KEY
import io.quartic.assertThrows
import io.quartic.tracker.UserDirectory
import io.quartic.tracker.api.RegistrationRequest
import io.quartic.tracker.api.RegistrationResponse
import io.quartic.tracker.model.UserId
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.security.PublicKey
import java.util.*
import javax.ws.rs.BadRequestException
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.NotFoundException

class UsersResourceShould {
    private val directory = mock<UserDirectory>()
    private val resource = UsersResource(directory)

    @Test
    fun respond_with_404_if_trying_to_lookup_unrecognised_user() {
        whenever(directory.getUser(any())).thenReturn(null)

        assertThrows<NotFoundException> { resource.getUser(mock()) }
    }

    @Test
    fun respond_with_404_if_trying_to_delete_unrecognised_user() {
        whenever(directory.deleteUser(any())).thenReturn(false)

        assertThrows<NotFoundException> { resource.deleteUser(mock()) }
    }

    @Test
    fun marshal_registration_request_and_response() {
        val id = UserId(123)
        whenever(directory.registerUser(any(), any())).thenReturn(id)

        assertThat(resource.registerUser(RegistrationRequest("foo", base64Encode(EC_PUBLIC_KEY.encoded))),
                equalTo(RegistrationResponse(id.toString())))
        val captor = argumentCaptor<PublicKey>()
        verify(directory).registerUser(eq("foo"), captor.capture())
        assertThat(captor.firstValue.encoded, equalTo(EC_PUBLIC_KEY.encoded))
    }

    @Test
    fun respond_with_401_if_trying_to_register_with_unrecognised_code() {
        whenever(directory.registerUser(any(), any())).thenReturn(null)

        assertThrows<NotAuthorizedException> { resource.registerUser(RegistrationRequest("foo", base64Encode(EC_PUBLIC_KEY.encoded))) }
    }

    @Test
    fun respond_with_400_if_trying_to_register_with_invalid_key() {
        assertThrows<BadRequestException> { resource.registerUser(RegistrationRequest("foo", "abcdefg")) }
    }

    @Test
    fun respond_with_400_if_trying_to_register_with_invalid_key_type() {
        assertThrows<BadRequestException> { resource.registerUser(RegistrationRequest("foo", base64Encode(RSA_PUBLIC_KEY.encoded))) }
    }

    private fun base64Encode(bytes: ByteArray) = Base64.getEncoder().encodeToString(bytes)
}