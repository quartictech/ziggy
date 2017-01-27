package io.quartic.tracker

import com.google.cloud.datastore.testing.LocalDatastoreHelper
import io.quartic.EC_PUBLIC_KEY
import io.quartic.tracker.model.RegisteredUser
import io.quartic.tracker.model.UnregisteredUser
import io.quartic.tracker.model.User
import io.quartic.tracker.model.UserId
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.joda.time.Duration
import org.junit.After
import org.junit.Before
import org.junit.Test

class UserDirectoryShould {
    private val helper = LocalDatastoreHelper.create()
    private lateinit var directory: UserDirectory

    @Before
    fun before() {
        helper.start()
        directory = UserDirectory(helper.options.service)
    }

    @After
    fun after() {
        helper.stop(Duration.millis(3000))
    }

    @Test
    fun respect_namespace_isolation() {
        val anotherDirectory = UserDirectory(helper.options
                .toBuilder()
                .setNamespace("something-else")
                .build().service)

        directory.createUser()
        assertThat(anotherDirectory.getUsers().entries, empty())
    }

    @Test
    fun report_created_users() {
        val idA = directory.createUser()
        val idB = directory.createUser()

        assertThat(directory.getUsers(), allOf(hasKey(idA), hasKey(idB)))
    }

    @Test
    fun create_unregistered_users() {
        val id = directory.createUser()

        assertThat(directory.getUsers()[id], instanceOf(UnregisteredUser::class.java))
    }

    @Test
    fun create_unique_registration_codes() {
        val users = listOf(
                directory.getUser(directory.createUser()) as UnregisteredUser,
                directory.getUser(directory.createUser()) as UnregisteredUser,
                directory.getUser(directory.createUser()) as UnregisteredUser,
                directory.getUser(directory.createUser()) as UnregisteredUser,
                directory.getUser(directory.createUser()) as UnregisteredUser
        )

        assertThat(users.distinctBy { it.registrationCode }, hasSize(users.size))
    }

    @Test
    fun lookup_valid_user() {
        val id = directory.createUser()
        val user = directory.getUser(id) as UnregisteredUser

        assertThat(user.id, equalTo(id))
    }

    @Test
    fun return_null_if_trying_to_lookup_unrecognised_user() {
        assertThat(directory.getUser(UserId(123)), nullValue())
    }

    @Test
    fun no_longer_report_deleted_users() {
        val id = directory.createUser()
        directory.deleteUser(id)

        assertThat(directory.getUsers().entries, empty())
    }

    @Test
    fun return_false_if_trying_to_delete_unrecognised_user() {
        assertThat(directory.deleteUser(UserId(123)), equalTo(false))
    }

    @Test
    fun report_registered_user_once_user_is_registered() {
        val user = directory.getUser(directory.createUser()) as UnregisteredUser
        directory.registerUser(user.registrationCode, EC_PUBLIC_KEY)

        assertThat(directory.getUser(user.id), equalTo(RegisteredUser(user.id, EC_PUBLIC_KEY) as User))
    }

    @Test
    fun return_null_if_trying_to_register_with_unrecognised_code() {
        val user = directory.getUser(directory.createUser()) as UnregisteredUser

        assertThat(directory.registerUser(user.registrationCode + "X", EC_PUBLIC_KEY), nullValue())
    }

    @Test
    fun return_null_if_trying_to_register_same_user_twice() {
        val user = directory.getUser(directory.createUser()) as UnregisteredUser
        directory.registerUser(user.registrationCode, EC_PUBLIC_KEY)

        assertThat(directory.registerUser(user.registrationCode, EC_PUBLIC_KEY), nullValue())
    }
}