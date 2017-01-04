package io.quartic.tracker.resource

import io.quartic.common.logging.logger
import io.quartic.tracker.api.UploadRequest
import org.slf4j.Logger
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/upload")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class UploadResource {
    private val LOG: Logger by logger()

    @POST
    fun upload(request: UploadRequest): Response {
        LOG.info("request: ${request.values.size}")
        return Response.ok().build()
    }
}