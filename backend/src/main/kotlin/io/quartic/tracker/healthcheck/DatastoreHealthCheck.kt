package io.quartic.tracker.healthcheck

import com.codahale.metrics.health.HealthCheck
import com.google.cloud.datastore.Datastore

class DatastoreHealthCheck(private val datastore: Datastore) : HealthCheck() {
    // Any old key will do
    private val key = datastore.newKeyFactory()
            .setKind("healthcheck")
            .newKey("healthcheck")

    override fun check(): Result {
        datastore.get(key)      // So long as this doesn't throw, this means the service is reachable
        return Result.healthy()
    }
}