package io.quartic.tracker.resource

import io.quartic.common.logging.logger
import io.quartic.tracker.api.UploadRequest
import io.dropwizard.auth.Auth
import io.quartic.tracker.Store
import io.quartic.tracker.model.User
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType


@Path("/upload")
@Consumes(MediaType.APPLICATION_JSON)   // TODO: should consider protobuf
@Produces(MediaType.APPLICATION_JSON)
class UploadResource(val store: Store) {
    private val LOG by logger()

    @POST
    fun postStuff(@Auth user: User, stuff: UploadRequest) {
        LOG.info("User '${user.id} uploaded: $stuff")
    }
}