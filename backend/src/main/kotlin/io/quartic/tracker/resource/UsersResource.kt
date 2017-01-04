package io.quartic.tracker.resource

import io.quartic.tracker.Store
import io.quartic.tracker.api.RegistrationRequest
import io.quartic.tracker.api.RegistrationResponse
import io.quartic.tracker.model.User
import io.quartic.tracker.model.UserId
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class UsersResource(val store: Store) {
    // TODO: most of these methods should be exposed to the outside world!  (Perhaps use Dropwizard @RolesAllowed)

    @GET
    fun getUsers(): Map<UserId, User> = store.getUsers()

    @GET
    @Path("/{id}")
    fun getUser(@PathParam("id") userId: UserId) = store.getUser(userId) ?: throw NotFoundException("User '$userId' not recognised")

    @DELETE
    @Path("/{id}")
    fun deleteUser(@PathParam("id") userId: UserId) = if (store.deleteUser(userId)) Unit else throw NotFoundException("User '$userId' not recognised")

    @POST
    fun createUser() = store.createUser()

    @POST
    @Path("/register")
    fun registerUser(request: RegistrationRequest): RegistrationResponse {
        val userId = store.registerUser(request.code, request.publicKey)
        return if (userId != null) RegistrationResponse(userId.toString()) else throw NotAuthorizedException("Unrecognised code ${request.code}")
    }
}
