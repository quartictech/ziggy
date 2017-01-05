package io.quartic.tracker.auth

import io.dropwizard.auth.AuthFilter
import io.quartic.common.logging.logger
import io.quartic.tracker.Store
import io.quartic.tracker.model.User
import io.quartic.tracker.model.UserId
import java.util.*
import javax.ws.rs.WebApplicationException
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.SecurityContext

class ClientSignatureAuthFilter : AuthFilter<ClientSignatureCredentials, User>() {
    private val LOG by logger()
    private val regex = """$PREFIX userId="(.+)", signature="(.+)"""".toRegex()

    override fun filter(requestContext: ContainerRequestContext) {
        val credentials = extractCredentials(requestContext)
        if (!authenticate(requestContext, credentials, SecurityContext.CLIENT_CERT_AUTH)) {
            throw WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm))
        }
    }

    private fun extractCredentials(requestContext: ContainerRequestContext): ClientSignatureCredentials? {
        if (requestContext.method != "POST" && requestContext.method != "PUT") {
            LOG.warn("Unsupported method '${requestContext.method}'")
            return null
        }

        val authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)
        if (authHeader == null) {
            LOG.warn("Authorization header is missing")
            return null
        }

        val matchResult = regex.matchEntire(authHeader)
        if (matchResult == null) {
            LOG.warn("Authorization header format invalid")
            return null
        }

        val signature = try {
            Base64.getDecoder().decode(matchResult.groupValues[2])
        } catch (e: IllegalArgumentException) {
            LOG.warn("Undecodable signature (${e.message})")
            return null
        }

        return ClientSignatureCredentials(
                UserId(matchResult.groupValues[1]),
                signature,
                extractEntity(requestContext)
        )
    }

    /** Convert the entity to a ByteArray. */
    private fun extractEntity(requestContext: ContainerRequestContext): ByteArray {
        val output = requestContext.entityStream.readBytes()
        requestContext.entityStream = output.inputStream()  // Reset for subsequent readers
        return output
    }

    companion object {
        fun create(store: Store): ClientSignatureAuthFilter = create(ClientSignatureAuthenticator(store))

        fun create(authenticator: ClientSignatureAuthenticator): ClientSignatureAuthFilter {
            val builder = object : AuthFilterBuilder<ClientSignatureCredentials, User, ClientSignatureAuthFilter>() {
                override fun newInstance() = ClientSignatureAuthFilter()
            }

            return builder
                    .setAuthenticator(authenticator)
                    .setPrefix(PREFIX)
                    .buildAuthFilter()
        }

        val PREFIX = "QuarticAuth"
    }
}