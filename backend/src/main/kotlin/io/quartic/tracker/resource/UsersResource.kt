package io.quartic.tracker.resource

import io.quartic.common.logging.logger
import io.quartic.common.uid.UidGenerator
import io.quartic.common.uid.randomGenerator
import io.quartic.tracker.api.RegistrationRequest
import io.quartic.tracker.api.RegistrationResponse
import io.quartic.tracker.model.RegisteredUser
import io.quartic.tracker.model.UnregisteredUser
import io.quartic.tracker.model.User
import io.quartic.tracker.model.UserId
import java.util.*
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class UsersResource {
    private val LOG by logger()

    private val random = Random()
    private val uidGen: UidGenerator<UserId> = randomGenerator(::UserId)
    private val users = mutableMapOf<UserId, User>()
    private val unregisteredUsers = mutableMapOf<String, UnregisteredUser>()    // Keys are registration codes

    // TODO: need to persist the user state

    // TODO: none of these methods should be exposed to the outside world!

    @GET
    fun getUsers(): Map<UserId, User> = synchronized { HashMap(users) }

    @GET
    @Path("/{id}")
    fun getUser(@PathParam("id") userId: UserId) = synchronized {
        users[userId] ?: throw NotFoundException("No user with ID $userId")
    }

    @DELETE
    @Path("/{id}")
    fun deleteUser(@PathParam("id") userId: UserId) = synchronized {
        val user = users.remove(userId) ?: throw NotFoundException("No user with ID $userId")
        if (user is UnregisteredUser) {
            unregisteredUsers.remove(user.registrationCode)
        }
    }

    @POST
    fun createUser(): UserId = synchronized {
        val user = UnregisteredUser(uidGen.get(), generateCode())
        users[user.id] = user
        unregisteredUsers[user.registrationCode] = user
        user.id
    }

    @POST
    @Path("/register")
    fun registerUser(request: RegistrationRequest) = synchronized {
        val user = unregisteredUsers[request.code] ?: throw NotAuthorizedException("Unrecognised code ${request.code}")
        unregisteredUsers.remove(request.code)
        users[user.id] = RegisteredUser(user.id, request.publicKey)
        LOG.info("Registered user ${user.id}")
        RegistrationResponse(user.id.toString())
    }

    private fun generateCode() = (random.nextInt(9000) + 1000).toString()

    private fun <R> synchronized(block: () -> R): R {
        return synchronized(this, block)
    }
}
