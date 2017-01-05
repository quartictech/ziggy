package io.quartic.tracker.resource

import io.dropwizard.auth.Auth
import io.quartic.common.logging.logger
import io.quartic.tracker.Store
import io.quartic.tracker.model.UnregisteredUser
import io.quartic.tracker.model.User
import io.quartic.tracker.model.UserId
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/upload")
@Consumes(MediaType.APPLICATION_JSON)   // TODO: should consider protobuf
@Produces(MediaType.APPLICATION_JSON)
class UploadResource(val store: Store) {
    private val LOG by logger()

    @GET
    fun getStuff(@Auth user: User) : Unit {
        LOG.info("getStuff ($user)")
    }

    @POST
    fun postStuff(@Auth user: User, stuff: String) : Unit {
        LOG.info("postStuff ($user, $stuff)")
    }

    @POST
    @Path("/{userId}")
    fun postStuff(@PathParam("userId") userId: UserId) {
        val user = store.getUser(userId)

        when (user) {
            null -> throw NotFoundException("User '$userId' not recognised")
            is UnregisteredUser -> throw NotAuthorizedException("User '$userId' not registered")
        }
    }
}