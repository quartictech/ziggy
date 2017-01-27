package io.quartic.tracker

import com.google.cloud.datastore.DatastoreOptions
import com.google.cloud.pubsub.PubSubOptions
import io.dropwizard.auth.AuthDynamicFeature
import io.dropwizard.auth.AuthValueFactoryProvider
import io.dropwizard.setup.Environment
import io.quartic.common.application.ApplicationBase
import io.quartic.tracker.TrackerConfiguration.DatastoreConfiguration
import io.quartic.tracker.auth.ClientSignatureAuthFilter
import io.quartic.tracker.healthcheck.DatastoreHealthCheck
import io.quartic.tracker.healthcheck.PubSubTopicHealthCheck
import io.quartic.tracker.model.User
import io.quartic.tracker.resource.UploadResource
import io.quartic.tracker.resource.UsersResource
import java.time.Clock

class TrackerApplication : ApplicationBase<TrackerConfiguration>() {
    override fun runApplication(configuration: TrackerConfiguration, environment: Environment) {
        val pubsub = PubSubOptions.getDefaultInstance().service
        val datastore = datastore(configuration.datastore)

        val directory = UserDirectory(datastore)
        val publisher = Publisher(pubsub, configuration.pubsub.topic!!)

        val metrics = environment.metrics()
        with (environment.jersey()) {
            register(AuthDynamicFeature(ClientSignatureAuthFilter.create(directory, configuration.signatureVerificationEnabled)))
            register(AuthValueFactoryProvider.Binder(User::class.java))
            register(UsersResource(directory))
            register(UploadResource(publisher, Clock.systemUTC(), metrics))
        }

        with (environment.healthChecks()) {
            register("topic", PubSubTopicHealthCheck(pubsub, configuration.pubsub.topic!!))
            register("datastore", DatastoreHealthCheck(datastore))
        }
    }

    private fun datastore(config: DatastoreConfiguration) = DatastoreOptions.getDefaultInstance()
            .toBuilder()
            .setNamespace(config.namespace)
            .build()
            .service

    companion object {
        @JvmStatic fun main(args: Array<String>) = TrackerApplication().run(*args)
    }
}