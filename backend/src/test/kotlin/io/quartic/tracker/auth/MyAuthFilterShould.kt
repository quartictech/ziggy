package io.quartic.tracker.auth

import com.nhaarman.mockito_kotlin.*
import io.quartic.tracker.model.UserId
import io.quartic.tracker.resource.MyCredentials
import org.junit.jupiter.api.Test
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.HttpHeaders

class MyAuthFilterShould {
    private val authenticator = mock<MyAuthenticator>()
    private val filter = MyAuthFilter.create(authenticator)

    @Test
    fun extract_null_creds_if_no_auth_header() {
        swallowExceptions {
            filter.filter(mockRequestContext(null))
        }

        verify(authenticator, never()).authenticate(any())
    }

    @Test
    fun extract_null_creds_if_scheme_incorrect() {
        swallowExceptions {
            filter.filter(mockRequestContext("Basic 123456"))
        }

        verify(authenticator, never()).authenticate(any())
    }

    @Test
    fun extract_null_creds_if_params_invalid() {
        swallowExceptions {
            filter.filter(mockRequestContext("QuarticAuth userId=\"abc\", xjhgafj=\"def\""))
        }

        verify(authenticator, never()).authenticate(any())
    }


    @Test
    fun extract_auth_header_fields() {
        // TODO: and request

        swallowExceptions {
            filter.filter(mockRequestContext("QuarticAuth userId=\"abc\", signature=\"789\""))
        }

        verify(authenticator).authenticate(MyCredentials(UserId("abc"), "789", "blahblah"))
    }

    private fun <R> swallowExceptions(block: () -> R): R? {
        try {
            return block()
        } catch (e: Exception) {
            return null
        }
    }


    // TODO: change this to getHeaders(HttpHeaders.AUTHORIZATION).getFirst() or whatever
    private fun mockRequestContext(authHeaderValue: String?): ContainerRequestContext {
        val requestContext = mock<ContainerRequestContext> {
            on { getHeaderString(HttpHeaders.AUTHORIZATION) } doReturn(authHeaderValue)
        }
        return requestContext
    }
}