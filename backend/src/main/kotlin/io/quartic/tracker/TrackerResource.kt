package io.quartic.tracker

import io.quartic.common.logging.logger
import io.quartic.tracker.api.RegistrationRequest
import io.quartic.tracker.api.RegistrationResponse
import io.quartic.tracker.api.UploadRequest
import java.util.*
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

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

    @POST
    @Path("/upload")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun upload(request: UploadRequest): Response {
        val response = Response.ok().build()
        LOG.info("$request -> $response")
        return response
    }

}
