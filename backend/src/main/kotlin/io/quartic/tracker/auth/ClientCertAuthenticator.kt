package io.quartic.tracker.auth

import io.dropwizard.auth.Authenticator
import io.quartic.common.logging.logger
import io.quartic.tracker.Store
import io.quartic.tracker.auth.ClientCertCredentials
import java.util.*

class ClientCertAuthenticator(val store: Store): Authenticator<ClientCertCredentials, MyPrincipal> {
    private val LOG by logger()

    override fun authenticate(credentials: ClientCertCredentials): Optional<MyPrincipal> {
        LOG.info("authenticate ($credentials)")
        // TODO
        return Optional.of(MyPrincipal())
    }
}