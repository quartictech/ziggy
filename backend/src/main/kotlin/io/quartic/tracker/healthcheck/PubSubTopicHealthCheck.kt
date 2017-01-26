package io.quartic.tracker.healthcheck

import com.codahale.metrics.health.HealthCheck
import com.google.cloud.pubsub.PubSub

class PubSubTopicHealthCheck(
        private val pubsub: PubSub,
        private val topicName: String
) : HealthCheck() {
    override fun check(): Result = if (pubsub.getTopic(topicName) != null) {
        Result.healthy()
    } else {
        Result.unhealthy("Topic '$topicName' does not exist")
    }
}