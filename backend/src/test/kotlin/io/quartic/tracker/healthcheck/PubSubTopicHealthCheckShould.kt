package io.quartic.tracker.healthcheck

import com.google.cloud.pubsub.Topic
import com.google.cloud.pubsub.testing.LocalPubSubHelper
import org.joda.time.Duration
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PubSubTopicHealthCheckShould {
    private val helper = LocalPubSubHelper.create()
    private val pubsub = helper.options.service
    private val healthcheck = PubSubTopicHealthCheck(pubsub, "myTopic")

    @After
    fun after() {
        try {
            helper.stop(Duration.millis(3000))
        } catch (e: Exception) { }
    }

    @Test
    fun report_healthy_if_pubsub_alive_and_topic_exists() {
        helper.start()
        pubsub.create(Topic.of("myTopic"))

        assertTrue(healthcheck.execute().isHealthy)
    }

    @Test
    fun report_unhealthy_if_pubsub_alive_but_topic_doesnt_exist() {
        helper.start()

        assertFalse(healthcheck.execute().isHealthy)
    }

    @Test
    fun report_unhealthy_if_pubsub_not_alive() {
        assertFalse(healthcheck.execute().isHealthy)
    }
}