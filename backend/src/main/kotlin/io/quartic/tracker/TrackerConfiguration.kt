package io.quartic.tracker

import io.dropwizard.Configuration

class TrackerConfiguration : Configuration() {
    class DatastoreConfiguration {
        var emulated = false
        var projectId: String? = null
        var namespace: String? = null
    }

    val datastore = DatastoreConfiguration()
}


