package io.quartic.tracker.auth

import io.dropwizard.auth.AuthFilter
import io.quartic.common.logging.logger
import io.quartic.tracker.Store
import io.quartic.tracker.model.UserId
import javax.ws.rs.WebApplicationException
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.SecurityContext

class ClientCertAuthFilter : AuthFilter<ClientCertCredentials, MyPrincipal>() {
    private val LOG by logger()
    private val regex = """$PREFIX userId="(.+)", signature="(.+)"""".toRegex()

    override fun filter(requestContext: ContainerRequestContext) {
        val credentials = extractCredentials(requestContext)
        if (!authenticate(requestContext, credentials, SecurityContext.CLIENT_CERT_AUTH)) {
            throw WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm))
        }
    }

    private fun extractCredentials(requestContext: ContainerRequestContext): ClientCertCredentials? {
        if (requestContext.method != "POST" && requestContext.method != "PUT") {
            LOG.warn("Unsupported method '${requestContext.method}")
            return null
        }

        val authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION) ?: return null
        val matchResult = regex.matchEntire(authHeader) ?: return null
        val entity = extractEntity(requestContext)

        return ClientCertCredentials(UserId(matchResult.groupValues[1]), matchResult.groupValues[2], entity)
    }

    /** Convert the entity to a ByteArray. */
    private fun extractEntity(requestContext: ContainerRequestContext): ByteArray {
        val output = requestContext.entityStream.readBytes()
        requestContext.entityStream = output.inputStream()  // Reset for subsequent readers
        return output
    }

    companion object {
        fun create(store: Store): ClientCertAuthFilter = create(ClientCertAuthenticator(store))

        fun create(authenticator: ClientCertAuthenticator): ClientCertAuthFilter {
            val builder = object : AuthFilterBuilder<ClientCertCredentials, MyPrincipal, ClientCertAuthFilter>() {
                override fun newInstance() = ClientCertAuthFilter()
            }

            return builder
                    .setAuthenticator(authenticator)
                    .setPrefix(PREFIX)
                    .buildAuthFilter()
        }

        val PREFIX = "QuarticAuth"
    }
}