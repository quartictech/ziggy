package io.quartic.tracker

import com.nhaarman.mockito_kotlin.mock
import io.quartic.tracker.model.RegisteredUser
import io.quartic.tracker.model.UnregisteredUser
import io.quartic.tracker.model.User
import io.quartic.tracker.model.UserId
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import java.security.PublicKey

class StoreShould {
    private val store = Store()

    @Test
    fun report_created_users() {
        val idA = store.createUser()
        val idB = store.createUser()

        assertThat(store.getUsers(), allOf(hasKey(idA), hasKey(idB)))
    }

    @Test
    fun create_unregistered_users() {
        val id = store.createUser()

        assertThat(store.getUsers()[id], instanceOf(UnregisteredUser::class.java))
    }

    @Test
    fun create_unique_registration_codes() {
        val users = listOf(
                store.getUser(store.createUser()) as UnregisteredUser,
                store.getUser(store.createUser()) as UnregisteredUser,
                store.getUser(store.createUser()) as UnregisteredUser,
                store.getUser(store.createUser()) as UnregisteredUser,
                store.getUser(store.createUser()) as UnregisteredUser
        )

        assertThat(users.distinctBy { it.registrationCode }, hasSize(users.size))
    }

    @Test
    fun lookup_valid_user() {
        val id = store.createUser()
        val user = store.getUser(id) as UnregisteredUser

        assertThat(user.id, equalTo(id))
    }

    @Test
    fun return_null_if_trying_to_lookup_unrecognised_user() {
        assertThat(store.getUser(UserId("abc")), nullValue())
    }

    @Test
    fun no_longer_report_deleted_users() {
        val id = store.createUser()
        store.deleteUser(id)

        assertThat(store.getUsers().entries, empty())
    }

    @Test
    fun return_false_if_trying_to_delete_unrecognised_user() {
        assertThat(store.deleteUser(UserId("abc")), equalTo(false))
    }

    @Test
    fun report_registered_user_once_user_is_registered() {
        val user = store.getUser(store.createUser()) as UnregisteredUser
        val publicKey = mock<PublicKey>()
        store.registerUser(user.registrationCode, publicKey)

        assertThat(store.getUser(user.id), equalTo(RegisteredUser(user.id, publicKey) as User))
    }

    @Test
    fun return_null_if_trying_to_register_with_unrecognised_code() {
        val user = store.getUser(store.createUser()) as UnregisteredUser

        assertThat(store.registerUser(user.registrationCode + "X", mock()), nullValue())
    }

    @Test
    fun return_null_if_trying_to_register_same_user_twice() {
        val user = store.getUser(store.createUser()) as UnregisteredUser
        store.registerUser(user.registrationCode, mock())

        assertThat(store.registerUser(user.registrationCode, mock()), nullValue())
    }
}