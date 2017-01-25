package io.quartic.tracker.resource

import com.nhaarman.mockito_kotlin.*
import io.quartic.common.test.assertThrows
import io.quartic.common.serdes.encode
import io.quartic.tracker.Publisher
import io.quartic.tracker.api.SensorValue
import io.quartic.tracker.api.UploadRequest
import io.quartic.tracker.model.Message
import io.quartic.tracker.model.User
import io.quartic.tracker.model.UserId
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import javax.ws.rs.ServerErrorException

class UploadResourceShould {
    private val publisher = mock<Publisher>()
    private val clock = Clock.fixed(Instant.EPOCH, ZoneId.systemDefault())
    private val resource = UploadResource(publisher, clock)

    @Test
    fun publish_uploaded_sensor_data_along_with_metadata() {
        val request = uploadRequest()

        resource.upload(user(123), request)

        verify(publisher).publish(encode(Message(
                userId = UserId(123),
                timestamp = clock.instant(),
                data = request
        )))
    }

    @Test
    internal fun respond_with_500_if_publishing_fails() {
        whenever(publisher.publish(any())).doThrow(RuntimeException("sad"))

        assertThrows<ServerErrorException> { resource.upload(user(123), uploadRequest()) }
    }

    private fun user(userId: Long): User = mock {
        on { id } doReturn UserId(userId)
    }

    private fun uploadRequest() = UploadRequest(
            System.currentTimeMillis(),
            0,
            0,
            listOf(
                    SensorValue(45, "Alice", "Foo", 9876),
                    SensorValue(67, "Bob", "Bar", 5432)
            ))
}