package io.quartic.tracker.auth

import io.dropwizard.auth.AuthFilter
import io.quartic.tracker.Store
import io.quartic.tracker.model.UserId
import io.quartic.tracker.resource.MyCredentials
import javax.ws.rs.WebApplicationException
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.SecurityContext

class MyAuthFilter : AuthFilter<MyCredentials, MyPrincipal>() {
    private val regex = """$prefix\s+userId="(.*)", signature="(.*)"""".toRegex()

    override fun filter(requestContext: ContainerRequestContext) {
        val credentials = extractCreds(requestContext)
        if (!authenticate(requestContext, credentials, SecurityContext.CLIENT_CERT_AUTH)) {
            throw WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm))
        }
    }

    private fun extractCreds(requestContext: ContainerRequestContext): MyCredentials? {
        val authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION) ?: return null
        val matchResult = regex.matchEntire(authHeader) ?: return null

        return MyCredentials(UserId("foo"), "signature", "request")
    }

    companion object {
        fun create(store: Store): MyAuthFilter = create(MyAuthenticator(store))

        fun create(authenticator: MyAuthenticator): MyAuthFilter {
            val builder = object : AuthFilterBuilder<MyCredentials, MyPrincipal, MyAuthFilter>() {
                override fun newInstance() = MyAuthFilter()
            }

            return builder
                    .setAuthenticator(authenticator)
                    .setPrefix("QuarticAuth")
                    .buildAuthFilter()
        }
    }
}