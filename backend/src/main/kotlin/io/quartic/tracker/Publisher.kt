package io.quartic.tracker

import com.google.cloud.pubsub.Message
import com.google.cloud.pubsub.PubSub
import com.google.cloud.pubsub.PubSubException
import io.quartic.common.logging.logger

class Publisher(private val pubsub: PubSub, private val topic: String) {
    private val LOG by logger()

    fun publish(data: String): String {
        try {
            return pubsub.publish(topic, Message.of(data))
        } catch (e: PubSubException) {
            // TODO: If certain error conditions (e.g. topic doesn't exist), then take action and retry
            throw e
        }
    }
}
