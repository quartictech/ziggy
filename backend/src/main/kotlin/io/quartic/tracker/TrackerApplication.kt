package io.quartic.tracker

import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreOptions
import com.google.cloud.datastore.testing.LocalDatastoreHelper
import io.dropwizard.auth.AuthDynamicFeature
import io.dropwizard.auth.AuthValueFactoryProvider
import io.dropwizard.lifecycle.Managed
import io.dropwizard.setup.Environment
import io.quartic.common.application.ApplicationBase
import io.quartic.tracker.TrackerConfiguration.DatastoreConfiguration
import io.quartic.tracker.auth.ClientSignatureAuthFilter
import io.quartic.tracker.model.User
import io.quartic.tracker.resource.UploadResource
import io.quartic.tracker.resource.UsersResource
import org.joda.time.Duration

class TrackerApplication : ApplicationBase<TrackerConfiguration>() {
    override fun runApplication(configuration: TrackerConfiguration, environment: Environment) {
        val store = Store(datastore(configuration.datastore, environment))

        with (environment.jersey()) {
            register(AuthDynamicFeature(ClientSignatureAuthFilter.create(store)))
            register(AuthValueFactoryProvider.Binder(User::class.java))
            register(UsersResource(store))
            register(UploadResource(store))
        }
    }

    // TODO: It would be better to run Datastore emulator independently (via Gradle or something), and inject configuration
    private fun datastore(config: DatastoreConfiguration, environment: Environment): Datastore {
        if (config.emulated) {
            val managedHelper = ManagedDatastoreHelper()
            environment.lifecycle().manage(managedHelper)
            return managedHelper.options.service
        } else {
            return DatastoreOptions.newBuilder()
                    .setProjectId(config.projectId)
                    .setNamespace(config.namespace)
                    .build()
                    .service
        }
    }

    private class ManagedDatastoreHelper : Managed {
        private val helper = LocalDatastoreHelper.create()

        val options: DatastoreOptions
            get() = helper.options

        override fun start() {
            helper.start()
        }

        override fun stop() {
            helper.stop(Duration.millis(3000))
        }
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) = TrackerApplication().run(*args)
    }
}