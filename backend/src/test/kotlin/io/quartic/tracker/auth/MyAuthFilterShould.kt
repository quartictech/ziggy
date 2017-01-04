package io.quartic.tracker.auth

import com.nhaarman.mockito_kotlin.*
import io.quartic.assertThrows
import io.quartic.tracker.model.UserId
import io.quartic.tracker.resource.MyCredentials
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import java.io.InputStream
import java.util.*
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.WebApplicationException
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.HttpHeaders

class MyAuthFilterShould {
    private val authenticator = mock<MyAuthenticator>()
    private val filter = MyAuthFilter.create(authenticator)
    private val requestContext = mock<ContainerRequestContext>(RETURNS_DEEP_STUBS)

    @Test
    fun extract_null_creds_if_no_auth_header() {
        mockMethod("POST")

        assertThrows<WebApplicationException> { filter.filter(requestContext) }
        verify(authenticator, never()).authenticate(any())
    }

    @Test
    fun extract_null_creds_if_scheme_incorrect() {
        mockMethod("POST")
        mockAuthHeader("Basic 123456")

        assertThrows<WebApplicationException> { filter.filter(requestContext) }
        verify(authenticator, never()).authenticate(any())
    }

    @Test
    fun extract_null_creds_if_params_invalid() {
        mockMethod("POST")
        mockAuthHeader("QuarticAuth userId=\"abc\", xjhgafj=\"def\"")

        assertThrows<WebApplicationException> { filter.filter(requestContext) }
        verify(authenticator, never()).authenticate(any())
    }

    @Test
    fun extract_creds_based_on_auth_header_params_and_request_body() {
        mockValidHeaderForMethod("POST")
        mockAuthorized()

        filter.filter(requestContext)

        verify(authenticator).authenticate(MyCredentials(UserId("abc"), "789", "stuff and nonsense".toByteArray()))
    }

    @Test
    fun reject_if_authenticator_rejects() {
        mockValidHeaderForMethod("POST")
        // Didn't call mockAuthorized()

        assertThrows<WebApplicationException> { filter.filter(requestContext) }
    }

    @Test
    fun support_put() {
        mockValidHeaderForMethod("PUT")
        mockAuthorized()

        filter.filter(requestContext)
    }

    @Test
    fun reject_get() {
        mockValidHeaderForMethod("GET")
        mockAuthorized()

        assertThrows<WebApplicationException> { filter.filter(requestContext) }
    }

    // The entity InputStream in real-life is not resettable
    @Test
    fun reset_entity_stream_without_actually_calling_reset() {
        val originalStream: InputStream = "hello".toByteArray().inputStream()
        mockValidHeaderForMethod("POST")
        mockAuthorized()
        whenever(requestContext.entityStream).thenReturn(originalStream)

        filter.filter(requestContext)

        val captor = argumentCaptor<InputStream>()
        verify(requestContext).entityStream = captor.capture()
        assertThat(captor.firstValue, not(sameInstance(originalStream)))            // Ensure we're not cheating
        assertThat(captor.firstValue.readBytes(), equalTo("hello".toByteArray()))   // Ensure still readable
    }

    private fun mockValidHeaderForMethod(method: String) {
        mockMethod(method)
        mockAuthHeader("QuarticAuth userId=\"abc\", signature=\"789\"")
        mockBody("stuff and nonsense")
    }

    private fun mockMethod(method: String) {
        whenever(requestContext.method).thenReturn(method)
    }

    private fun mockAuthHeader(value: String) {
        whenever(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn(value)
    }

    private fun mockBody(value: String) {
        whenever(requestContext.entityStream).thenReturn(value.toByteArray().inputStream())
    }

    private fun mockAuthorized() {
        whenever(authenticator.authenticate(any())).thenReturn(Optional.of(mock()))
    }
}