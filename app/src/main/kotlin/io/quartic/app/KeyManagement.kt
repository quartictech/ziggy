package io.quartic.app

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.*
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PublicKey
import java.security.spec.ECGenParameterSpec

fun generateKeyPair() {
    // TODO: validate that keys are being stored in hardware
    try {
        val kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM_EC, "AndroidKeyStore")
        kpg.initialize(KeyGenParameterSpec.Builder(KEY_ALIAS, PURPOSE_SIGN)
                .setDigests(DIGEST_SHA256)
                .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                .build())
        kpg.generateKeyPair()
    } catch (e: Exception) {
        throw RuntimeException("Could not generate key pair", e) // TODO: what's a better error-handling approach?
    }
}

// TODO: what's a better error-handling approach?
val publicKey: PublicKey
    get() {
        try {
            val ks = KeyStore.getInstance("AndroidKeyStore")
            ks.load(null)
            return ks.getCertificate(KEY_ALIAS).publicKey
        } catch (e: Exception) {
            throw RuntimeException("Could not acquire public key", e)
        }
    }

private val KEY_ALIAS = "key"

// TODO: method to sign something
