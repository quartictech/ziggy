package io.quartic.cartan;

import com.fasterxml.jackson.module.kotlin.*
import com.google.cloud.pubsub.PubSub
import com.google.common.collect.ImmutableMap
import io.quartic.common.logging.logger
import io.quartic.common.serdes.OBJECT_MAPPER
import io.quartic.tracker.api.FusedLocationSensorValue
import io.quartic.tracker.api.Message
import io.quartic.tracker.api.UserId
import rx.Emitter
import rx.Observable
import java.util.*

object MessageProcessor {
    val LOG by logger()

    data class TimestampedValue<T>(val timestamp: Long, val value: T)
    data class Coordinates(val lat: Double, val long: Double)

    data class Entity (
            val id: UserId,
            val location: TimestampedValue<Coordinates>? = null
    )

    fun messages(subscription: String, pubsub: PubSub): Observable<Message> {
        return Observable.fromEmitter({ emitter ->
            pubsub.pullAsync(subscription,
                    { msg ->
                        LOG.info("received message ${msg.id}")
                        try {
                            emitter.onNext(OBJECT_MAPPER.readValue(msg.payloadAsString))
                        } catch (e: Exception) {
                            LOG.error(e.message)
                            throw e
                        }
                    },
                    arrayOf())
        }, Emitter.BackpressureMode.BUFFER)
    }

    fun states(messages: Observable<Message>): Observable<Map<UserId, Entity>> {
        val initialState: Map<UserId, Entity> = ImmutableMap.of<UserId, Entity>()
        return messages
                .doOnNext { LOG.info("$it") }
                .scan(initialState, { state, message -> updateState(state, message) })
    }

    private fun updateState(state: Map<UserId, Entity>, message: Message): Map<UserId, Entity> {
        val map = HashMap(state)
        map.put(message.userId, handleEntity(state.getOrElse(message.userId, { -> Entity(message.userId) }), message))

        return ImmutableMap.copyOf(map)
    }

    private fun handleEntity(state: Entity, message: Message): Entity {
        var entity = state
        message.data.values.forEach { sensorReading ->
            entity = when(sensorReading.value) {
                is FusedLocationSensorValue -> fusedLocationUpdate(entity, sensorReading.timestamp,
                        sensorReading.value as FusedLocationSensorValue)
                else -> entity
            }
        }
        return entity
    }

    private fun fusedLocationUpdate(entity: MessageProcessor.Entity, timestamp: Long, value: FusedLocationSensorValue): MessageProcessor.Entity {
        if (entity.location == null || timestamp > entity.location.timestamp) {
            return entity.copy(location = TimestampedValue(
                    timestamp,
                    Coordinates(value.lat, value.long))
            )
        }
        return entity
    }
}
