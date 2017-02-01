package io.quartic.app.crypto

import android.util.Base64
import io.quartic.app.state.ApplicationState
import io.quartic.common.core.SignatureUtils
import java.security.KeyFactory
import java.security.KeyPair
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

class KeyManager(private val state: ApplicationState) {

    private val keyFactory = KeyFactory.getInstance(SignatureUtils.ALGORITHM)

    fun generateKeyPairIfMissing() {
        if (!isKeyPairPresent()) {
            val keyPair = SignatureUtils.generateRSAKeyPair()
            state.encodedPublicKey = Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP)
            state.encodedPrivateKey = Base64.encodeToString(keyPair.private.encoded, Base64.NO_WRAP)
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
                    keyFactory.generatePublic(X509EncodedKeySpec(Base64.decode(state.encodedPublicKey!!, Base64.NO_WRAP))),
                    keyFactory.generatePrivate(PKCS8EncodedKeySpec(Base64.decode(state.encodedPrivateKey!!, Base64.NO_WRAP)))
            )
        } else {
            throw RuntimeException("Key pair has not been generated yet")
        }
    }

    private fun isKeyPairPresent() = (state.encodedPublicKey != null && state.encodedPrivateKey != null)
}

