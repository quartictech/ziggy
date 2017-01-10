package io.quartic.tracker.resource


import io.dropwizard.auth.Auth
import io.quartic.common.logging.logger
import io.quartic.tracker.api.UploadRequest
import io.quartic.tracker.model.User
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/upload")
@Consumes(MediaType.APPLICATION_JSON)   // TODO: should consider protobuf
@Produces(MediaType.APPLICATION_JSON)
class UploadResource() {
    private val LOG by logger()

    @POST
    fun upload(@Auth user: User, request: UploadRequest): Response {
        LOG.info("User '${user.id} uploaded: ${request.values.size}")
        return Response.ok().build()
    }
}