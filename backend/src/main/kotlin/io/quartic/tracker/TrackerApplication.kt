package io.quartic.tracker

import io.dropwizard.auth.AuthDynamicFeature
import io.dropwizard.auth.AuthValueFactoryProvider
import io.dropwizard.setup.Environment
import io.quartic.common.application.ApplicationBase
import io.quartic.tracker.auth.ClientSignatureAuthFilter
import io.quartic.tracker.model.User
import io.quartic.tracker.resource.UploadResource
import io.quartic.tracker.resource.UsersResource

class TrackerApplication : ApplicationBase<TrackerConfiguration>() {
    override fun runApplication(configuration: TrackerConfiguration, environment: Environment) {
        val store = Store()
        with (environment.jersey()) {
            register(AuthDynamicFeature(ClientSignatureAuthFilter.create(store)))
            register(AuthValueFactoryProvider.Binder(User::class.java))
            register(UsersResource(store))
            register(UploadResource(store))
        }
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) = TrackerApplication().run(*args)
    }
}