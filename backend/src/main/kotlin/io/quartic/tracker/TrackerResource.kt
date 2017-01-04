package io.quartic.tracker

import io.quartic.common.logging.logger
import io.quartic.tracker.api.RegistrationRequest
import io.quartic.tracker.api.RegistrationResponse
import java.util.*
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

class TrackerResource {
    private val LOG by logger()

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun register(request: RegistrationRequest): RegistrationResponse {
        val response = RegistrationResponse(UUID.randomUUID().toString().substring(0, 6))
        LOG.info("$request -> $response")
        return response
    }
}
