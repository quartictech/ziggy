package io.quartic.tracker

import com.google.cloud.datastore.DatastoreOptions
import com.google.cloud.datastore.testing.LocalDatastoreHelper
import com.google.cloud.pubsub.PubSubOptions
import io.dropwizard.auth.AuthDynamicFeature
import io.dropwizard.auth.AuthValueFactoryProvider
import io.dropwizard.setup.Environment
import io.quartic.common.application.ApplicationBase
import io.quartic.tracker.TrackerConfiguration.DatastoreConfiguration
import io.quartic.tracker.auth.ClientSignatureAuthFilter
import io.quartic.tracker.common.managedEmulatorFor
import io.quartic.tracker.model.User
import io.quartic.tracker.resource.UploadResource
import io.quartic.tracker.resource.UsersResource
import java.time.Clock

class TrackerApplication : ApplicationBase<TrackerConfiguration>() {
    override fun runApplication(configuration: TrackerConfiguration, environment: Environment) {
        // Google Cloud service wrappers
        val directory = UserDirectory(datastore(configuration.datastore, environment))
        val publisher = Publisher(PubSubOptions.getDefaultInstance().service, configuration.pubsub.topic!!)

        with (environment.jersey()) {
            register(AuthDynamicFeature(ClientSignatureAuthFilter.create(directory, configuration.signatureVerificationEnabled)))
            register(AuthValueFactoryProvider.Binder(User::class.java))
            register(UsersResource(directory))
            register(UploadResource(publisher, Clock.systemUTC()))
        }
    }

    // TODO: It would be better to run emulators independently (via Gradle or something), and inject configuration somehow

    private fun datastore(config: DatastoreConfiguration, environment: Environment) = if (config.emulated) {
        managedEmulatorFor(LocalDatastoreHelper.create(), environment)
    } else {
        DatastoreOptions.getDefaultInstance()
                .toBuilder()
                .setNamespace(config.namespace)
                .build()
                .service
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) = TrackerApplication().run(*args)
    }
}