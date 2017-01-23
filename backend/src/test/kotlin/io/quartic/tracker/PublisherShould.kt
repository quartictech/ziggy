package io.quartic.tracker

import com.google.cloud.pubsub.Subscription
import com.google.cloud.pubsub.SubscriptionInfo
import com.google.cloud.pubsub.TopicInfo
import com.google.cloud.pubsub.testing.LocalPubSubHelper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.joda.time.Duration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PublisherShould {
    private val helper = LocalPubSubHelper.create()
    private lateinit var publisher: Publisher
    private lateinit var subscription: Subscription

    @BeforeEach
    fun before() {
        helper.start()
        val pubsub = helper.options.service

        pubsub.create(TopicInfo.of(TOPIC))

        subscription = pubsub.create(SubscriptionInfo.of(TOPIC, "test"))
        publisher = Publisher(pubsub, TOPIC)
    }

    @AfterEach
    fun after() {
        helper.stop(Duration.millis(3000))
    }

    @Test
    fun publish_when_basically_regular() {
        publisher.publish("Hello")

        assertThat(subscription.pull(1).next().payloadAsString, equalTo("Hello"))
    }

    companion object {
        val TOPIC = "whatever"
    }
}