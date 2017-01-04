package io.quartic.tracker

import io.quartic.tracker.api.RegistrationRequest
import io.quartic.tracker.model.RegisteredUser
import io.quartic.tracker.model.UnregisteredUser
import io.quartic.tracker.model.User
import io.quartic.tracker.model.UserId
import org.hamcrest.Matchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.NotFoundException

class UsersResourceShould {
    val resource = UsersResource()

    @Test
    fun report_created_users() {
        val idA = resource.createUser()
        val idB = resource.createUser()

        assertThat(resource.getUsers(), allOf(hasKey(idA), hasKey(idB)))
    }

    @Test
    fun create_unregistered_users() {
        val id = resource.createUser()

        assertThat(resource.getUsers()[id], instanceOf(UnregisteredUser::class.java))
    }

    @Test
    fun create_unique_registration_codes() {
        val users = listOf(
                resource.getUser(resource.createUser()) as UnregisteredUser,
                resource.getUser(resource.createUser()) as UnregisteredUser,
                resource.getUser(resource.createUser()) as UnregisteredUser,
                resource.getUser(resource.createUser()) as UnregisteredUser,
                resource.getUser(resource.createUser()) as UnregisteredUser
        )

        assertThat(users.distinctBy { it.registrationCode }, hasSize(users.size))
    }

    @Test
    fun lookup_valid_user() {
        val id = resource.createUser()
        val user = resource.getUser(id) as UnregisteredUser

        assertThat(user.id, equalTo(id))
    }

    @Test(expected = NotFoundException::class)
    fun respond_with_404_if_trying_to_lookup_unrecognised_user() {
        resource.getUser(UserId("abc"))
    }

    @Test
    fun no_longer_report_deleted_users() {
        val id = resource.createUser()
        resource.deleteUser(id)

        assertThat(resource.getUsers().entries, empty())
    }

    @Test(expected = NotFoundException::class)
    fun respond_with_404_if_trying_to_delete_unrecognised_user() {
        resource.deleteUser(UserId("abc"))
    }

    @Test
    fun report_registered_user_once_user_is_registered() {
        val user = resource.getUser(resource.createUser()) as UnregisteredUser
        resource.registerUser(RegistrationRequest(user.registrationCode, "abcdefg"))

        assertThat(resource.getUser(user.id), equalTo(RegisteredUser(user.id, "abcdefg") as User))
    }

    @Test(expected = NotAuthorizedException::class)
    fun respond_with_401_if_trying_to_register_with_unrecognised_code() {
        val user = resource.getUser(resource.createUser()) as UnregisteredUser
        resource.registerUser(RegistrationRequest(user.registrationCode + "X", "abcdefg"))
    }

    @Test(expected = NotAuthorizedException::class)
    fun respond_with_401_if_trying_to_register_same_user_twice() {
        val user = resource.getUser(resource.createUser()) as UnregisteredUser
        resource.registerUser(RegistrationRequest(user.registrationCode, "abcdefg"))
        resource.registerUser(RegistrationRequest(user.registrationCode, "abcdefg"))
    }
}