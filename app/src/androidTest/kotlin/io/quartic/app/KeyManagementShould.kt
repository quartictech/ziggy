package io.quartic.app

import android.content.Context
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Before
import android.support.test.InstrumentationRegistry
import io.quartic.common.core.SignatureUtils
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.*

@RunWith(AndroidJUnit4::class)
@SmallTest
class KeyManagementShould() {
    private lateinit var instrumentationCtx: Context

    @Before
    fun setup() {
        instrumentationCtx = InstrumentationRegistry.getTargetContext()
        deleteKey()
    }

    @Test
    fun generate_a_key() {
        assertThat(isKeyPresent(), equalTo(false))
        generateKeyPair19(instrumentationCtx)
        assertThat(isKeyPresent(), equalTo(true))
    }

    @Test
    fun sign_compatible_with_backend() {
        generateKeyPair19(instrumentationCtx)
        val data = "hello there".toByteArray()
        val signature = sign(data)!!
        val valid = SignatureUtils.verify(publicKey, data, signature)
        assertThat(valid, equalTo(true))
    }


}
