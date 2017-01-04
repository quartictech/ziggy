package io.quartic.tracker.auth

import io.dropwizard.auth.Authenticator
import io.quartic.common.logging.logger
import io.quartic.tracker.Store
import io.quartic.tracker.auth.ClientSignatureCredentials
import io.quartic.tracker.model.RegisteredUser
import io.quartic.tracker.model.UnregisteredUser
import java.util.*

class ClientSignatureAuthenticator(val store: Store): Authenticator<ClientSignatureCredentials, MyPrincipal> {
    private val LOG by logger()

    override fun authenticate(credentials: ClientSignatureCredentials): Optional<MyPrincipal> {
        val user = store.getUser(credentials.userId)
        when (user) {
            is RegisteredUser -> {
                // TODO: perform signature verification

                return Optional.of(MyPrincipal())
            }
            is UnregisteredUser -> {
                LOG.warn("User '${credentials.userId}' not registered")
                return Optional.empty()
            }
            null -> {
                LOG.warn("User '${credentials.userId}' not recognised")
                return Optional.empty()
            }
            else -> {
                throw RuntimeException("Unexpected user type '${user.javaClass}")
            }
        }
    }
}