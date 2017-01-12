package io.quartic.tracker

import com.google.cloud.pubsub.Message
import com.google.cloud.pubsub.PubSub
import com.google.cloud.pubsub.PubSubException

class Publisher(private val pubsub: PubSub, private val topic: String) {
    @Throws(PubSubException::class)
    fun publish(data: String): String {
        return pubsub.publish(topic, Message.of(data))
    }
}
