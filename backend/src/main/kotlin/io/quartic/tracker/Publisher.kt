package io.quartic.tracker

import com.google.cloud.pubsub.Message
import com.google.cloud.pubsub.PubSubException
import com.google.cloud.pubsub.Topic
import io.quartic.common.logging.logger

class Publisher(private val topic: () -> Topic) {
    private val LOG by logger()

    fun publish(data: String): String {
        try {
            return topic().publish(Message.of(data))
        } catch (e: PubSubException) {
            // TODO: If certain error conditions (e.g. topic doesn't exist), then take action and retry
            throw e
        }
    }
}
