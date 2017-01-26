package io.quartic.tracker.healthcheck

import com.google.cloud.datastore.testing.LocalDatastoreHelper
import org.joda.time.Duration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DatastoreHealthCheckShould {
    private val helper = LocalDatastoreHelper.create()
    private val datastore = helper.options.service
    private val healthcheck = DatastoreHealthCheck(datastore)

    @AfterEach
    fun after() {
        try {
            helper.stop(Duration.millis(3000))
        } catch (e: Exception) { }
    }

    @Test
    fun report_healthy_if_datastore_reachable() {
        helper.start()

        assertTrue(healthcheck.execute().isHealthy)
    }

    @Test
    fun report_unhealthy_if_datastore_not_reachable() {
        assertFalse(healthcheck.execute().isHealthy)
    }
}