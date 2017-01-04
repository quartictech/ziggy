package io.quartic.tracker.resource

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.quartic.assertThrows
import io.quartic.tracker.Store
import io.quartic.tracker.model.UnregisteredUser
import org.junit.jupiter.api.Test
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.NotFoundException

class UploadResourceShould {
    private val store = mock<Store>()
    private val resource = UploadResource(store)

    @Test
    fun respond_with_404_if_unrecognised_user() {
        whenever(store.getUser(any())).thenReturn(null)

        assertThrows<NotFoundException> { resource.postStuff(mock()) }
    }

    @Test
    fun respond_with_401_if_unregistered_user() {
        whenever(store.getUser(any())).thenReturn(mock<UnregisteredUser>())

        assertThrows<NotAuthorizedException> { resource.postStuff(mock()) }
    }


}