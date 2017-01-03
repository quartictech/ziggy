package io.quartic.tracker

import io.dropwizard.setup.Environment
import io.quartic.common.application.ApplicationBase

class TrackerApplication : ApplicationBase<TrackerConfiguration>() {
    override fun runApplication(configuration: TrackerConfiguration, environment: Environment) {
        environment.jersey().register(TrackerResource())
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) = TrackerApplication().run(*args)
    }
}