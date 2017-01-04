package io.quartic.tracker

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

class Store {
    private val LOG by logger()

    private val random = Random()
    private val uidGen: UidGenerator<UserId> = randomGenerator(::UserId)
    private val users = mutableMapOf<UserId, User>()
    private val unregisteredUsers = mutableMapOf<String, UnregisteredUser>()    // Keys are registration codes

    // TODO: need to persist the user state


    fun getUsers(): Map<UserId, User> = synchronized { HashMap(users) }

    fun getUser(userId: UserId) = synchronized { users[userId] }

    fun deleteUser(userId: UserId) = synchronized {
        val user = users.remove(userId)
        if (user != null) {
            if (user is UnregisteredUser) {
                unregisteredUsers.remove(user.registrationCode)
            }
            true
        } else {
            false
        }
    }

    fun createUser() = synchronized {
        val user = UnregisteredUser(uidGen.get(), generateCode())
        users[user.id] = user
        unregisteredUsers[user.registrationCode] = user
        user.id
    }

    fun registerUser(code: String, base64EncodedKey: String) = synchronized {
        val user = unregisteredUsers[code]
        if (user != null) {
            unregisteredUsers.remove(code)
            users[user.id] = RegisteredUser(user.id, base64EncodedKey)
            LOG.info("Registered user ${user.id}")
            user.id
        } else {
            null
        }
    }

    private fun generateCode() = (random.nextInt(9000) + 1000).toString()

    private fun <R> synchronized(block: () -> R): R {
        return synchronized(this, block)
    }
}