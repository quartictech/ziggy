#!/usr/bin/env python

from google.cloud import pubsub

if __name__ == "__main__":
    ps = pubsub.Client()

    topic = ps.topic("tracker.devtest.quartic.io")
    topic.create()

    subscription = topic.subscription("scribe.devtest.quartic.io")
    subscription.create()