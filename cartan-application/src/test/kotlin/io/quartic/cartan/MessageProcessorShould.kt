package io.quartic.cartan

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.quartic.tracker.api.*
import org.junit.Test
import rx.Observable.just
import rx.observers.TestSubscriber

class MessageProcessorShould {
    @Test
    fun should_process_states() {
        val messages = just(message("1", 51.0, 0.0, 0), message("1", 51.5, 0.5, 1))
        val states = MessageProcessor.states(messages)
        val test = TestSubscriber.create<Map<UserId, MessageProcessor.Entity>>()
        states.subscribe(test)
        test.assertValues(
                mapOf(),
                mapOf(Pair(UserId("1"), entity("1", 51.0, 0.0, 0))),
                mapOf(Pair(UserId("1"), entity("1", 51.5, 0.5, 1)))
        )
    }

    private fun message(user: String, lat: Double, long: Double, timestamp: Long): Message {
        val msg = mock<Message> {
            on { userId } doReturn(UserId(user))
            on { data } doReturn(UploadRequest(0, 0, "test", 0, 0,
                    listOf(SensorReading(0, "location", FusedLocationSensorValue(lat, long), timestamp)),
                    DeviceInformation("wat", "wat", "wat")))
        }
        return msg
    }

    private fun entity(userId: String, lat: Double, long: Double, timestamp:Long) = MessageProcessor.Entity(
            UserId("1"),
            location=MessageProcessor.TimestampedValue(timestamp, MessageProcessor.Coordinates(lat, long))
    )

}
