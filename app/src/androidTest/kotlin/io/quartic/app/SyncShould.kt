package io.quartic.app

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.quartic.app.state.ApplicationState
import io.quartic.app.sync.forceSync
import io.quartic.app.sync.getBatteryLevel
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class SyncShould {
    private lateinit var instrumentationCtx: Context
    private lateinit var applicationState: ApplicationState

    @Before
    fun setup() {
        instrumentationCtx = InstrumentationRegistry.getTargetContext()
        applicationState = ApplicationState(instrumentationCtx,
                ApplicationConfiguration.load(instrumentationCtx))

        generateKeyPairIfMissing(instrumentationCtx)
        applicationState.clear()
        applicationState.userId = "1"
    }

    @Test
    fun get_battery_level() {
        val batteryLevel = getBatteryLevel(instrumentationCtx)
        assertThat(batteryLevel, Matchers.greaterThanOrEqualTo(0))
    }
}
