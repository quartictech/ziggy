package io.quartic.app

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.quartic.app.sync.getBatteryLevel
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class SyncShould {
    private val instrumentationCtx = InstrumentationRegistry.getTargetContext()

    @Test
    fun get_battery_level() {
        val batteryLevel = getBatteryLevel(instrumentationCtx)
        assertThat(batteryLevel, greaterThanOrEqualTo(0))
    }
}
