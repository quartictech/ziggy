package io.quartic.tracker.auth

import io.dropwizard.auth.Authenticator
import io.quartic.common.logging.logger
import io.quartic.tracker.Store
import io.quartic.tracker.auth.ClientSignatureCredentials
import java.util.*

class ClientSignatureAuthenticator(val store: Store): Authenticator<ClientSignatureCredentials, MyPrincipal> {
    private val LOG by logger()

    override fun authenticate(credentials: ClientSignatureCredentials): Optional<MyPrincipal> {
        LOG.info("authenticate ($credentials)")
        // TODO
        return Optional.of(MyPrincipal())
    }
}