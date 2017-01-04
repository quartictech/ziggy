package io.quartic.tracker

import io.dropwizard.setup.Environment
import io.quartic.common.application.ApplicationBase
import io.quartic.tracker.resource.UsersResource

class TrackerApplication : ApplicationBase<TrackerConfiguration>() {
    override fun runApplication(configuration: TrackerConfiguration, environment: Environment) {
        environment.jersey().register(UsersResource())
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) = TrackerApplication().run(*args)
    }
}