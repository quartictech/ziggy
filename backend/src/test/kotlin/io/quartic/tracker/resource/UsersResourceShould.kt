package io.quartic.tracker.resource

import com.nhaarman.mockito_kotlin.*
import io.quartic.assertThrows
import io.quartic.tracker.Store
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
    // X.509 representation of valid EC public key
    private val ecKeyBytes = intArrayOf(0x30, 0x59, 0x30, 0x13, 0x06, 0x07, 0x2a, 0x86, 0x48, 0xce, 0x3d, 0x02, 0x01, 0x06, 0x08, 0x2a, 0x86, 0x48, 0xce, 0x3d, 0x03, 0x01, 0x07, 0x03, 0x42, 0x00, 0x04, 0xfa, 0x2f, 0xa9, 0x16, 0x5b, 0xcf, 0x87, 0x39, 0x8d, 0xca, 0xa2, 0x49, 0x4d, 0xcc, 0x97, 0x89, 0x05, 0xbc, 0x9a, 0x93, 0x9e, 0xf6, 0x36, 0x8c, 0x98, 0x26, 0xf0, 0x16, 0x4f, 0x03, 0x55, 0x58, 0x4b, 0x3b, 0x9c, 0xd1, 0x37, 0xa0, 0xd9, 0x29, 0x17, 0xb3, 0x9c, 0xb8, 0xf7, 0x20, 0x65, 0x7c, 0x73, 0x6d, 0x9e, 0xf3, 0xb8, 0x21, 0x0e, 0x9e, 0xf6, 0x2c, 0x64, 0x2c, 0xa4, 0x1b, 0xc8, 0xb4)
            .map(Int::toByte)
            .toByteArray()

    // X.509 representation of valid RSA public key
    private val rsaKeyBytes = intArrayOf(0x30, 0x5c, 0x30, 0x0d, 0x06, 0x09, 0x2a, 0x86, 0x48, 0x86, 0xf7, 0x0d, 0x01, 0x01, 0x01, 0x05, 0x00, 0x03, 0x4b, 0x00, 0x30, 0x48, 0x02, 0x41, 0x00, 0xaa, 0x1e, 0x8e, 0x9b, 0x9e, 0x0a, 0x99, 0xc6, 0x38, 0x8d, 0x1f, 0x3f, 0x83, 0x9b, 0xbf, 0x8f, 0xc7, 0x84, 0xd6, 0xd1, 0x37, 0x80, 0xe3, 0x65, 0x00, 0xc8, 0xfd, 0xc1, 0x6b, 0x18, 0x74, 0xf3, 0x27, 0x68, 0x68, 0x8f, 0x75, 0xef, 0xc3, 0xef, 0x89, 0x55, 0xc7, 0x80, 0x8d, 0x6e, 0xc8, 0x1e, 0xe9, 0x72, 0x90, 0x3a, 0x25, 0x36, 0x9d, 0x40, 0xc0, 0x43, 0x25, 0x42, 0x78, 0xa3, 0x6d, 0xd7, 0x02, 0x03, 0x01, 0x00, 0x01)
            .map(Int::toByte)
            .toByteArray()


    private val store = mock<Store>()
    private val resource = UsersResource(store)

    @Test
    fun respond_with_404_if_trying_to_lookup_unrecognised_user() {
        whenever(store.getUser(any())).thenReturn(null)

        assertThrows<NotFoundException> { resource.getUser(mock()) }
    }

    @Test
    fun respond_with_404_if_trying_to_delete_unrecognised_user() {
        whenever(store.deleteUser(any())).thenReturn(false)

        assertThrows<NotFoundException> { resource.deleteUser(mock()) }
    }

    @Test
    fun marshal_registration_request_and_response() {
        val id = UserId("abc")
        whenever(store.registerUser(any(), any())).thenReturn(id)

        assertThat(resource.registerUser(RegistrationRequest("foo", base64Encode(ecKeyBytes))),
                equalTo(RegistrationResponse(id.toString())))
        val captor = argumentCaptor<PublicKey>()
        verify(store).registerUser(eq("foo"), captor.capture())
        assertThat(captor.firstValue.encoded, equalTo(ecKeyBytes))
    }

    @Test
    fun respond_with_401_if_trying_to_register_with_unrecognised_code() {
        whenever(store.registerUser(any(), any())).thenReturn(null)

        assertThrows<NotAuthorizedException> { resource.registerUser(RegistrationRequest("foo", base64Encode(ecKeyBytes))) }
    }

    @Test
    fun respond_with_400_if_trying_to_register_with_invalid_key() {
        assertThrows<BadRequestException> { resource.registerUser(RegistrationRequest("foo", "abcdefg")) }
    }

    @Test
    fun respond_with_400_if_trying_to_register_with_invalid_key_type() {
        assertThrows<BadRequestException> { resource.registerUser(RegistrationRequest("foo", base64Encode(rsaKeyBytes))) }
    }

    private fun base64Encode(bytes: ByteArray) = Base64.getEncoder().encodeToString(bytes)
}