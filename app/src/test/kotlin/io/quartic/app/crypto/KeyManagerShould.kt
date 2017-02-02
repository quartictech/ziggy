package io.quartic.app.crypto

import com.nhaarman.mockito_kotlin.*
import io.quartic.app.state.ApplicationState
import io.quartic.common.core.SignatureUtils
import io.quartic.common.core.SignatureUtils.rsaKeyPairGenerator
import junit.framework.Assert.assertEquals
import org.hamcrest.Matchers
import org.junit.Assert.assertThat
import org.junit.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey

class KeyManagerShould {
    private val state = mock<ApplicationState>()
    private val kpg = mock<KeyPairGenerator>()
    private val manager = KeyManager(state, kpg)

    @Test
    fun generate_key_pair_if_none_exists() {
        mockNoKeyPairExists()
        val publicKey = mock<PublicKey> { on { encoded } doReturn byteArrayOf(1, 2, 3) }
        val privateKey = mock<PrivateKey> { on { encoded } doReturn byteArrayOf(4, 5, 6) }
        whenever(kpg.generateKeyPair()).thenReturn(KeyPair(publicKey, privateKey))

        manager.generateKeyPairIfMissing()

        verify(state).encodedPublicKey = byteArrayOf(1, 2, 3)
        verify(state).encodedPrivateKey = byteArrayOf(4, 5, 6)
    }

    @Test
    fun not_regenerate_key_pair_if_alredy_exists() {
        whenever(state.encodedPublicKey).thenReturn(byteArrayOf(1, 2, 3))
        whenever(state.encodedPrivateKey).thenReturn(byteArrayOf(4, 5, 6))

        manager.generateKeyPairIfMissing()

        verify(state, never()).encodedPrivateKey = any()
        verify(state, never()).encodedPublicKey = any()
    }

    @Test(expected = RuntimeException::class)
    fun throw_if_requesting_public_key_before_created() {
        mockNoKeyPairExists()

        manager.publicKey
    }

    @Test
    fun return_generated_public_key() {
        val kpg = rsaKeyPairGenerator().generateKeyPair()
        whenever(state.encodedPublicKey).thenReturn(kpg.public.encoded)
        whenever(state.encodedPrivateKey).thenReturn(kpg.private.encoded)

        assertEquals(kpg.public, manager.publicKey)
    }

    @Test(expected = RuntimeException::class)
    fun throw_if_requesting_signature_before_created() {
        mockNoKeyPairExists()

        manager.sign(byteArrayOf(1, 2, 3))
    }

    @Test
    fun generate_valid_signature() {
        val kpg = rsaKeyPairGenerator().generateKeyPair()
        whenever(state.encodedPublicKey).thenReturn(kpg.public.encoded)
        whenever(state.encodedPrivateKey).thenReturn(kpg.private.encoded)

        val signature = manager.sign(byteArrayOf(7, 8, 9))
        val expected = SignatureUtils.sign(kpg.private, byteArrayOf(7, 8, 9))

        assertThat(signature, Matchers.equalTo(expected))
    }

    private fun mockNoKeyPairExists() {
        whenever(state.encodedPublicKey).thenReturn(byteArrayOf())
        whenever(state.encodedPrivateKey).thenReturn(byteArrayOf())
    }
}