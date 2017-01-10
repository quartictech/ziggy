package io.quartic.tracker.auth

import io.dropwizard.auth.Authenticator
import io.quartic.common.logging.logger
import io.quartic.tracker.UserDirectory
import io.quartic.tracker.model.RegisteredUser
import io.quartic.tracker.model.UnregisteredUser
import io.quartic.tracker.model.User
import java.security.Signature
import java.security.SignatureException
import java.util.*

class ClientSignatureAuthenticator(val directory: UserDirectory): Authenticator<ClientSignatureCredentials, User> {
    private val LOG by logger()

    override fun authenticate(credentials: ClientSignatureCredentials): Optional<User> {
        val user = directory.getUser(credentials.userId)
        when (user) {
            is RegisteredUser -> {
                if (verifySignature(credentials, user)) {
                    return Optional.of(user)
                } else {
                    LOG.warn("Signature mismatch for '${credentials.userId}'")
                }
            }
            is UnregisteredUser -> LOG.warn("User '${credentials.userId}' not registered")
            null -> LOG.warn("User '${credentials.userId}' not recognised")
            else -> throw RuntimeException("Unexpected user type '${user.javaClass}")
        }

        return Optional.empty()
    }

    private fun verifySignature(credentials: ClientSignatureCredentials, user: RegisteredUser): Boolean {
        val verificationFunction = Signature.getInstance("SHA256withECDSA")
        verificationFunction.initVerify(user.publicKey)
        verificationFunction.update(credentials.request)
        try {
            return (verificationFunction.verify(credentials.signature))
        } catch (e: SignatureException) {
            LOG.warn("Invalid signature for '${user.id}'", e)
            return false
        }
    }
}