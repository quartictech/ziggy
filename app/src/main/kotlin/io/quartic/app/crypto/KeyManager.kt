package io.quartic.app.crypto

import io.quartic.app.state.ApplicationState
import io.quartic.common.core.SignatureUtils
import io.quartic.common.core.SignatureUtils.rsaKeyPairGenerator
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

class KeyManager(
        private val state: ApplicationState,
        private val keyPairGenerator: KeyPairGenerator = rsaKeyPairGenerator()
) {
    private val keyFactory = KeyFactory.getInstance(SignatureUtils.ALGORITHM)

    fun generateKeyPairIfMissing() {
        if (!isKeyPairPresent()) {
            val keyPair = keyPairGenerator.generateKeyPair()

            state.encodedPublicKey = keyPair.public.encoded
            state.encodedPrivateKey = keyPair.private.encoded
        }
    }

    fun deleteKey() {
        // TODO
    }

    val publicKey: PublicKey
        get() = getKeyPairOrThrow().public

    fun sign(data: ByteArray) = SignatureUtils.sign(getKeyPairOrThrow().private, data)

    private fun getKeyPairOrThrow(): KeyPair {
        if (isKeyPairPresent()) {
            return KeyPair(
                    keyFactory.generatePublic(X509EncodedKeySpec(state.encodedPublicKey)),
                    keyFactory.generatePrivate(PKCS8EncodedKeySpec(state.encodedPrivateKey))
            )
        } else {
            throw RuntimeException("Key pair has not been generated yet")
        }
    }

    private fun isKeyPairPresent() = (state.encodedPublicKey.isNotEmpty() && state.encodedPrivateKey.isNotEmpty())
}

