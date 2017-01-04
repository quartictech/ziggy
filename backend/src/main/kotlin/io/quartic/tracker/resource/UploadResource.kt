package io.quartic.tracker.resource

import io.dropwizard.auth.Auth
import io.quartic.common.logging.logger
import io.quartic.tracker.Store
import io.quartic.tracker.auth.MyPrincipal
import io.quartic.tracker.model.UnregisteredUser
import io.quartic.tracker.model.UserId
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/upload")
@Consumes(MediaType.APPLICATION_JSON)   // TODO: should consider protobuf
@Produces(MediaType.APPLICATION_JSON)
class UploadResource(val store: Store) {
    private val LOG by logger()

    @GET
    fun getStuff(@Auth principal: MyPrincipal) : Unit {
        LOG.info("getStuff ($principal)")
    }

    @POST
    fun postStuff(@Auth principal: MyPrincipal, stuff: String) : Unit {
        LOG.info("postStuff ($principal, $stuff)")
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