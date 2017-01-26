package io.quartic.tracker.common.metrics

import com.codahale.metrics.Meter
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Timer

fun <R : Any> R.meter(metrics: MetricRegistry, vararg names: String): Meter = metrics.meter(MetricRegistry.name(javaClass, *names))
fun <R : Any> R.timer(metrics: MetricRegistry, vararg names: String): Timer = metrics.timer(MetricRegistry.name(javaClass, *names))

