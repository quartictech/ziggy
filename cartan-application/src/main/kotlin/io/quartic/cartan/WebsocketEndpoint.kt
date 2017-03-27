package io.quartic.cartan;

import io.quartic.common.geojson.Feature
import io.quartic.common.geojson.FeatureCollection
import io.quartic.common.geojson.Point
import io.quartic.common.logging.logger
import io.quartic.common.serdes.OBJECT_MAPPER
import io.quartic.common.websocket.ResourceManagingEndpoint
import io.quartic.tracker.api.UserId
import rx.Observable
import rx.Subscription
import javax.websocket.Session

class WebsocketEndpoint(val observable: Observable<Map<UserId, MessageProcessor.Entity>>): ResourceManagingEndpoint<Subscription>() {
    data class LiveEvent (
            val updateType: String,
            val timestamp: Long,
            val featureCollection: FeatureCollection
    )

    companion object {
        val LOG by logger()
    }

    override fun createResourceFor(session: Session): Subscription = observable.subscribe { state ->
        LOG.info("received state: $state")
        session.asyncRemote.sendText(OBJECT_MAPPER.writeValueAsString(renderState(state)))
    }

    fun renderState(state: Map<UserId, MessageProcessor.Entity>): LiveEvent {
        val features = state
                .map { entry ->
                    val location = entry.value.location?.value
                    if (location != null) {
                        Feature(entry.key.toString(), Point(listOf(location.long, location.lat)))
                    }
                    else {
                        Feature(entry.key.toString(), null)
                    }
                }
        return LiveEvent("REPLACE", 0, FeatureCollection(features))
    }

    override fun releaseResource(resource: Subscription) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
