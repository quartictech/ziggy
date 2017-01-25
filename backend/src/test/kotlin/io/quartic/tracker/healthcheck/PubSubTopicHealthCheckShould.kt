package io.quartic.tracker.healthcheck

import com.google.cloud.pubsub.Topic
import com.google.cloud.pubsub.testing.LocalPubSubHelper
import org.joda.time.Duration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PubSubTopicHealthCheckShould {
    private val helper = LocalPubSubHelper.create()
    private val pubsub = helper.options.service
    private val healthcheck = PubSubTopicHealthCheck(pubsub, "myTopic")

    @AfterEach
    fun after() {
        try {
            helper.stop(Duration.millis(3000))
        } catch (e: Exception) { }
    }

    @Test
    fun report_healthy_if_pubsub_alive_and_topic_exists() {
        helper.start()
        pubsub.create(Topic.of("myTopic"))

        Assertions.assertTrue(healthcheck.execute().isHealthy)
    }

    @Test
    fun report_unhealthy_if_pubsub_alive_but_topic_doesnt_exist() {
        helper.start()

        Assertions.assertFalse(healthcheck.execute().isHealthy)
    }

    @Test
    fun report_unhealthy_if_pubsub_not_alive() {
        Assertions.assertFalse(healthcheck.execute().isHealthy)
    }
}