package io.quartic.tracker.resource

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.quartic.tracker.Store
import io.quartic.tracker.api.RegistrationRequest
import io.quartic.tracker.api.RegistrationResponse
import io.quartic.tracker.model.UserId
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.NotFoundException

class UsersResourceShould {
    val store = mock<Store>()
    val resource = UsersResource(store)

    @Test(expected = NotFoundException::class)
    fun respond_with_404_if_trying_to_lookup_unrecognised_user() {
        whenever(store.getUser(any())).thenReturn(null)

        resource.getUser(mock())
    }

    @Test(expected = NotFoundException::class)
    fun respond_with_404_if_trying_to_delete_unrecognised_user() {
        whenever(store.deleteUser(any())).thenReturn(false)

        resource.deleteUser(mock())
    }

    @Test
    fun marshal_registration_request_response() {
        val id = UserId("abc")
        whenever(store.registerUser(any(), any())).thenReturn(id)

        assertThat(resource.registerUser(RegistrationRequest("foo", "bar")), equalTo(RegistrationResponse(id.toString())))
        verify(store).registerUser("foo", "bar")
    }

    @Test(expected = NotAuthorizedException::class)
    fun respond_with_401_if_trying_to_register_with_unrecognised_code() {
        whenever(store.registerUser(any(), any())).thenReturn(null)

        resource.registerUser(RegistrationRequest("foo", "bar"))
    }
}