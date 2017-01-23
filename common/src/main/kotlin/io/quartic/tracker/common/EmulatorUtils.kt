package io.quartic.tracker.common

import com.google.cloud.Service
import com.google.cloud.ServiceOptions
import com.google.cloud.testing.BaseEmulatorHelper
import io.dropwizard.lifecycle.Managed
import io.dropwizard.setup.Environment
import io.quartic.common.logging.logger
import org.joda.time.Duration

fun <T : ServiceOptions<U, *, *>, U : Service<T>> managedEmulatorFor(
        helper: BaseEmulatorHelper<T>,
        environment: Environment,
        onStart: (U) -> Unit = {}
): U {
    val service = helper.options.getService()

    environment.lifecycle().manage(object : Managed {
        private val LOG by logger()

        override fun start() {
            LOG.info("Emulator ${helper.javaClass} starting")
            helper.start()
            onStart(service)
            LOG.info("Emulator ${helper.javaClass} started")
        }
        override fun stop() = helper.stop(Duration.millis(3000))
    })

    return service
}