package io.quartic.app

import android.util.Base64
import android.util.Log
import io.quartic.app.state.ApplicationState
import io.quartic.common.core.SignatureUtils
import java.security.KeyFactory
import java.security.KeyPair
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

class KeyManager(private val state: ApplicationState) {

    private val keyFactory = KeyFactory.getInstance(SignatureUtils.ALGORITHM)

    fun generateKeyPairIfMissing() {
        if (!isKeyPairPresent()) {
            val keyPair = SignatureUtils.generateRSAKeyPair()

            Log.i("KeyManager", keyPair.public.format)
            Log.i("KeyManager", keyPair.private.format)

            state.encodedPublicKey = Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP)
            state.encodedPrivateKey = Base64.encodeToString(keyPair.private.encoded, Base64.NO_WRAP)
        }
    }

    fun deleteKey() {
        // TODO
    }

    // TODO: what's a better error-handling approach?
    val publicKey: PublicKey
        get() = getKeyPairOrThrow().public

    fun sign(data: ByteArray) = SignatureUtils.sign(getKeyPairOrThrow().private, data)

    private fun getKeyPairOrThrow(): KeyPair {
        if (isKeyPairPresent()) {
            return KeyPair(
                    keyFactory.generatePublic(keySpecFrom(state.encodedPublicKey!!)),
                    keyFactory.generatePrivate(keySpecFrom(state.encodedPrivateKey!!))
            )
        } else {
            throw RuntimeException("Key pair has not been generated yet")
        }
    }

    private fun keySpecFrom(encodedKey: String) = X509EncodedKeySpec(Base64.decode(encodedKey, Base64.NO_WRAP))

    private fun isKeyPairPresent() = (state.encodedPublicKey != null && state.encodedPrivateKey != null)

}

