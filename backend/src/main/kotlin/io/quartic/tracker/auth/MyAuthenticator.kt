package io.quartic.tracker.auth

import io.dropwizard.auth.Authenticator
import io.quartic.common.logging.logger
import io.quartic.tracker.Store
import io.quartic.tracker.resource.MyCredentials
import java.util.*

class MyAuthenticator(val store: Store): Authenticator<MyCredentials, MyPrincipal> {
    private val LOG by logger()

    override fun authenticate(credentials: MyCredentials): Optional<MyPrincipal> {
        LOG.info("authenticate ($credentials)")
        // TODO
        return Optional.of(MyPrincipal())
    }
}