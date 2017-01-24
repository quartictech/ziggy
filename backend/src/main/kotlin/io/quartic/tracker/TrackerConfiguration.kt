package io.quartic.tracker

import io.dropwizard.Configuration

class TrackerConfiguration : Configuration() {
    class DatastoreConfiguration {
        var emulated = false
        var namespace: String? = null
    }

    class PubSubConfiguration {
        var emulated = false
        var topic: String? = null
    }

    val datastore = DatastoreConfiguration()
    val pubsub = PubSubConfiguration()
    var signatureVerificationEnabled = true
}


