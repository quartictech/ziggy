package io.quartic.tracker.resource

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.annotation.Metered
import io.dropwizard.auth.Auth
import io.quartic.common.logging.logger
import io.quartic.common.serdes.encode
import io.quartic.tracker.Publisher
import io.quartic.tracker.api.UploadRequest
import io.quartic.tracker.common.metrics.histogram
import io.quartic.tracker.model.Message
import io.quartic.tracker.model.User
import java.time.Clock
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/upload")
@Consumes(MediaType.APPLICATION_JSON)   // TODO: should consider protobuf
@Produces(MediaType.APPLICATION_JSON)
class UploadResource(
        private val publisher: Publisher,
        private val clock: Clock,
        metrics: MetricRegistry
) {
    private val LOG by logger()
    private val charsHistogram = histogram(metrics, "upload", "chars")

    @POST
    @Metered
    fun upload(@Auth user: User, request: UploadRequest): Int {
        try {
            val encoded = encode(Message(
                    userId = user.id,
                    timestamp = clock.instant(),
                    data = request
            ))
            val messageId = publisher.publish(encoded)
            charsHistogram.update(encoded.length)
            LOG.info("User '${user.id}' uploaded ${request.values.size} sensor reading(s) with messageId=$messageId")
            return request.values.size
        } catch (e: Exception) {
            throw ServiceUnavailableException("Could not publish sensor readings", 30, e)
        }
    }
}