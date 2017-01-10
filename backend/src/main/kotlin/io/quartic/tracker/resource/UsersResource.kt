package io.quartic.tracker.resource

import io.quartic.common.logging.logger
import io.quartic.tracker.UserDirectory
import io.quartic.tracker.api.RegistrationRequest
import io.quartic.tracker.api.RegistrationResponse
import io.quartic.tracker.model.User
import io.quartic.tracker.model.UserId
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class UsersResource(val store: UserDirectory) {
    private val LOG by logger()
    private val keyFactory = KeyFactory.getInstance("EC")

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
        val publicKey = try {
            decodeKey(request.publicKey)
        } catch (e: Exception) {
            LOG.warn("Invalid key", e)
            throw BadRequestException("Invalid key", e)
        }
        val userId = store.registerUser(request.code, publicKey)
        return if (userId != null) RegistrationResponse(userId.toString()) else throw NotAuthorizedException("Unrecognised code ${request.code}")
    }

    private fun decodeKey(base64EncodedKey: String): PublicKey {
        val publicKey = keyFactory.generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(base64EncodedKey)))
        if (publicKey.algorithm != EXPECTED_KEY_ALGORITHM) {
            throw IllegalArgumentException("Incorrect key algorithm '${publicKey.algorithm}'")
        }
        return publicKey
    }

    companion object {
        val EXPECTED_KEY_ALGORITHM = "EC"
    }
}
